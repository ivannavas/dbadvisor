package io.github.ivannavas.dbadvisor.h2.parsers;

import io.github.ivannavas.dbadvisor.core.items.Node;
import io.github.ivannavas.dbadvisor.core.items.Plan;
import io.github.ivannavas.dbadvisor.core.parsers.PlanParser;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class H2PlanParser implements PlanParser {

    private static final Pattern P_TABLE_SCAN   = Pattern.compile("(?i)\\bTABLE\\s+SCAN\\b(?:\\s+|:|\\s+ON\\s+)([\\w.]+)?");
    private static final Pattern P_TABLE_FILTER = Pattern.compile("(?i)\\bTABLE\\s+FILTER\\b(?:\\s+|:|\\s+ON\\s+)([\\w.]+)?");
    private static final Pattern P_INDEX_ON     = Pattern.compile("(?i)\\bINDEX\\b[^\\n]*?\\bON\\s+([\\w.]+)");
    private static final Pattern P_RELATION     = Pattern.compile("(?i)\\bFROM\\s+([\\w.]+)");
    private static final Pattern P_ALIAS_AS     = Pattern.compile("(?i)\\bAS\\s+([\\w]+)\\b");
    private static final Pattern P_ALIAS_BARE   = Pattern.compile("(?i)\\b([\\w]+)\\s*\\((?:alias|a)\\b"); // por si acaso
    private static final Pattern P_FILTER_INLINE= Pattern.compile("(?i)\\bWHERE\\s+(.+)$");
    private static final Pattern P_COST         = Pattern.compile("(?i)\\bcost\\s*([0-9]+(?:\\.[0-9]+)?)");
    private static final Pattern P_ROWS         = Pattern.compile("(?i)\\brows?\\s*([0-9]+)");

    @Override
    public Plan parse(String rawPlan) {
        if (rawPlan == null || rawPlan.isBlank()) {
            throw new IllegalArgumentException("Empty EXPLAIN plan");
        }

        List<String> lines = rawPlan
                .replace("\t", "    ")
                .replace("\r", "")
                .lines()
                .map(s -> s.replaceFirst("\\s+$", ""))
                .filter(s -> !s.isBlank())
                .toList();

        if (lines.isEmpty()) {
            throw new IllegalArgumentException("No content in EXPLAIN");
        }

        List<MNode> roots = new ArrayList<>();
        Deque<Frame> stack = new ArrayDeque<>();
        MNode lastNode = null;

        for (String rawLine : lines) {
            String line = stripExplainEcho(rawLine);
            int indent = countLeadingSpaces(line);
            String content = line.stripLeading();

            if (content.startsWith("(") && content.endsWith(")") && lastNode != null && (lastNode.filter == null || lastNode.filter.isBlank())) {
                String f = content.substring(1, content.length() - 1).trim();
                if (!f.isEmpty()) {
                    lastNode.filter = f;
                }
                continue;
            }

            if (isHeaderNoise(content)) continue;

            MNode current = parseLine(content);

            Matcher mWhere = P_FILTER_INLINE.matcher(content);
            if (mWhere.find() && (current.filter == null || current.filter.isBlank())) {
                current.filter = mWhere.group(1).trim();
            }

            while (!stack.isEmpty() && indent <= stack.peek().indent) {
                stack.pop();
            }
            if (stack.isEmpty()) {
                roots.add(current);
            } else {
                stack.peek().node.children.add(current);
            }
            stack.push(new Frame(indent, current));
            lastNode = current;
        }

        MNode root;
        if (roots.isEmpty()) {
            throw new IllegalArgumentException("No nodes found in EXPLAIN");
        } else if (roots.size() == 1) {
            root = roots.getFirst();
        } else {
            root = new MNode();
            root.type = Node.NodeType.UNKNOWN;
            root.children.addAll(roots);
        }

        return new Plan(toImmutable(root));
    }

    @Override
    public String getPlanGeneratorQuery(String query) {
        return "EXPLAIN " + query;
    }

    // ----------------- helpers -----------------

    private record Frame(int indent, MNode node) {
    }

    private static class MNode {
        Node.NodeType type = Node.NodeType.UNKNOWN;
        String relationName;
        String alias;
        String filter;
        Double startupCost;
        Double totalCost;
        Long rows;
        List<MNode> children = new ArrayList<>();
    }

    private static Node toImmutable(MNode m) {
        List<Node> kids = m.children.stream().map(H2PlanParser::toImmutable).toList();
        return new Node(m.type, m.relationName, m.alias, m.filter, m.startupCost, m.totalCost, m.rows, kids);
    }

    private static int countLeadingSpaces(String s) {
        int i = 0;
        while (i < s.length() && s.charAt(i) == ' ') i++;
        return i;
    }

    private static boolean isHeaderNoise(String content) {
        String c = content.toUpperCase(Locale.ROOT);
        return c.equals("SELECT") || c.equals("FROM") || c.equals("WHERE") || c.startsWith("/*") && c.endsWith("*/");
    }

    private static String stripExplainEcho(String line) {
        return line;
    }

    private static MNode parseLine(String content) {
        MNode n = new MNode();

        String upper = content.toUpperCase(Locale.ROOT);

        if (upper.contains("INDEX")) {
            n.type = Node.NodeType.INDEX_SCAN;
        }
        if (upper.contains("TABLE SCAN")) {
            n.type = Node.NodeType.SEQ_SCAN;
        }
        if (upper.contains("JOIN")) {
            n.type = Node.NodeType.JOIN;
        }
        if (upper.contains("AGGREGATE") || upper.contains("GROUP")) {
            n.type = Node.NodeType.AGGREGATE;
        }
        if (upper.contains("FILTER")) {
            if (n.type == Node.NodeType.UNKNOWN) {
                n.type = Node.NodeType.FILTER;
            }
        }
        if (n.type == Node.NodeType.UNKNOWN) {
            if (upper.startsWith("SCAN") || upper.contains("TABLE")) n.type = Node.NodeType.SEQ_SCAN;
        }

        n.relationName = firstMatch(content, P_TABLE_SCAN, 1);
        if (n.relationName == null) n.relationName = firstMatch(content, P_TABLE_FILTER, 1);
        if (n.relationName == null) n.relationName = firstMatch(content, P_INDEX_ON, 1);
        if (n.relationName == null) n.relationName = firstMatch(content, P_RELATION, 1);

        String alias = firstMatch(content, P_ALIAS_AS, 1);
        if (alias == null) alias = firstMatch(content, P_ALIAS_BARE, 1);
        n.alias = alias;

        n.totalCost = parseDouble(content, P_COST);
        n.startupCost = null;
        n.rows = parseLong(content, P_ROWS);

        n.filter = firstMatch(content, P_FILTER_INLINE, 1);

        return n;
    }

    private static String firstMatch(String s, Pattern p, int group) {
        Matcher m = p.matcher(s);
        if (m.find()) {
            String g = m.group(group);
            return g != null && !g.isBlank() ? g.trim() : null;
        }
        return null;
    }

    private static Double parseDouble(String s, Pattern p) {
        Matcher m = p.matcher(s);
        if (m.find()) {
            try { return Double.parseDouble(m.group(1)); } catch (NumberFormatException ignored) {}
        }
        return null;
    }

    private static Long parseLong(String s, Pattern p) {
        Matcher m = p.matcher(s);
        if (m.find()) {
            try { return Long.parseLong(m.group(1)); } catch (NumberFormatException ignored) {}
        }
        return null;
    }
}
