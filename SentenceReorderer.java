package MT_Project;

import java.util.Arrays;
import java.util.List;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.BufferedWriter;
import java.io.FileWriter;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.trees.*;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;

public class SentenceReorderer {

    public static void main(String[] args) {
        // this program reorders german sentences using the Stanford lexicalize parser
        // arg 0 is input file
        // arg 1 is output file
        // TODO: check that arguments exist and return error to user if arguments not given

        String parserModel = "edu/stanford/nlp/models/lexparser/germanPCFG.ser.gz";
        LexicalizedParser lp = LexicalizedParser.loadModel(parserModel);

        // open read file
        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader(args[0]));
        } catch (Exception e) {
            System.err.println("Something went wrong. " + e.getMessage());
            return;
        }

        // open output file
        BufferedWriter out;
        try {
            FileWriter fstream = new FileWriter(args[1], false); //false tells to overwrite file contents
            out = new BufferedWriter(fstream);
        } catch (Exception e) {
            System.err.println("Something went wrong. " + e.getMessage());
            return;
        }

        // read lines from file
        try {
            String line;
            while ((line = br.readLine()) != null) {
                String[] sent = line.split("\\s+");

                // do this stuff based on the ParserDemo example
                List<CoreLabel> rawWords = Sentence.toCoreLabelList(sent);
                Tree parse = lp.apply(rawWords);

                // apply rules
                SentenceReorderer.verb_initial(parse);
                SentenceReorderer.verb_second(parse);
                SentenceReorderer.subject(parse);
                SentenceReorderer.particles(parse);
                SentenceReorderer.infinitives(parse);
                SentenceReorderer.negation(parse);

                // save manipulated tree to new file as a tokenized sentence
                out.write(SentenceReorderer.get_sentence(parse));
                out.write("\n");

            }
            out.close();

        } catch (Exception e) {
            System.err.println("Something went wrong. " + e.getMessage());
        }


    }

    public static String get_sentence(Tree tree) {
        String result = "";
        List<Tree> leaves = tree.getLeaves();
        for ( Tree leaf : leaves ) {
            result += leaf.label() + " ";
        }
        return result;
    }

    /**
     * Transforms a tree using Rule 1: Verb Initial
     *
     * This rule finds all 'VP' nodes containing a Tree Head as a direct child.
     * The tree head is then moved to become the leftmost child in the 'VP' node.
     *
     * @param parse is the parse tree we are transforming
     */
    public static void verb_initial(Tree parse) {

        // recursively call this on the whole tree to perform rule on all VPs
        if ( ! parse.isLeaf() ) {
            Tree[] children = parse.children();
            for (Tree child : children) {
                SentenceReorderer.verb_initial(child);
            }
        }

        // do main logic of the rule here
        // rule will happen for every node in tree meeting criteria below
        if (parse.label().value().equals("VP")) {

            List<Tree> children = parse.getChildrenAsList();

            // find head
            int head_loc = SentenceReorderer.find_tree_head(parse);

            // insert head into first position in children array
            if (head_loc >= 0) {
                SentenceReorderer.move_node(children, head_loc, 0);
            }

            parse.setChildren(children);
        }
    }

    /**
     * Transforms tree using Rule 2: Verb Second
     *
     * This rule finds all 'S' nodes containing both a conditional node and a Tree Head
     * The tree head is moved so that it immediately follows the conditional.
     *
     * @param parse is the parse tree we are transforming
     */
    public static void verb_second(Tree parse) {

        // recursively call this on the whole tree
        if ( ! parse.isLeaf() ) {
            Tree[] children = parse.children();
            for (Tree child : children) {
                SentenceReorderer.verb_second(child);
            }
        }

        List<String> conjunction_tags = Arrays.asList("KOUS", "PWAV", "PWS", "PRELS", "PRELAT");

        // do main logic of the rule here
        // rule will happen for every node in tree meeting criteria below
        // for any S in the tree
        if (parse.label().value().equals("S")) {
            List<Tree> children = parse.getChildrenAsList();
            if (children.size() == 0) return;

            for (int i = 0; i < children.size(); i++) {
                Tree child = children.get(i);
                if (conjunction_tags.contains(child.label().value())) {
                    // find head
                    int head_loc = SentenceReorderer.find_tree_head(parse);

                    // insert head to immediately after conj
                    if (head_loc >= 0) {
                        SentenceReorderer.move_node(children, head_loc, i + 1);
                    }
                    break;
                }
            }
            parse.setChildren(children);
        }
    }

    public static void subject(Tree parse) {

        // recursively call this on the whole tree to perform rule on all Ss
        if ( ! parse.isLeaf() ) {
            Tree[] children = parse.children();
            for (Tree child : children) {
                SentenceReorderer.subject(child);
            }
        }

        // do main logic of the rule here
        // rule will happen for every node in tree meeting criteria below
        if (parse.label().value().equals("S")) {

            List<Tree> children = parse.getChildrenAsList();
            if (children.size() == 0) return;


            for (int i = 0; i < children.size(); i++) {
                Tree child = children.get(i);
                if (child.label().value().equals("NP") ||
                        child.label().value().equals("PPER")) {
                    // find head
                    int head_loc = SentenceReorderer.find_tree_head(parse);

                    // insert head to immediately after conj
                    if (head_loc >= 0) {
                        SentenceReorderer.move_node(children, i, head_loc);
                    }
                    break;
                }
            }

            // Tree subject = leftmost child in children where child.label = [NP or PPER]

            // insert subject into children at position immediately before head
            parse.setChildren(children);
        }

    }

    public static void particles(Tree parse) {
        // do main logic of the rule here
        // rule will happen for every node in tree meeting criteria below

        if (parse.label().value().equals("S")) {
            List<Tree> children = parse.getChildrenAsList();
            if (children.size() == 0) return;

            int v_loc = -1;
            for (int i = 0; i < children.size(); i++) {
                Tree child = children.get(i);
                if (child.label().value().equals("VVFIN") ||
                        child.label().value().equals("VAFIN") ||
                        child.label().value().equals("VMFIN") ) {
                    v_loc = i;
                    break;
                }
            }

            int p_loc = -1;
            for (int i = 0; i < children.size(); i++) {
                Tree child = children.get(i);
                if (child.label().value().equals("PTKVZ") ) {
                    p_loc = i;
                    break;
                }
            }

            // if (child.label == "VVFIN" or "VAFIN" or "VMFIN") AND (child.label == "PTKVZ") in children  {
            if (p_loc > 0 && v_loc > 0) {
                // move the PTKVZ child to immediately before V*FIN
                SentenceReorderer.move_node(children, p_loc, v_loc);
            }

            parse.setChildren(children);
        }

        // recursively call this on the whole tree to perform rule on all Ss
        if ( ! parse.isLeaf() ) {
            Tree[] children = parse.children();
            for (Tree child : children) {
                SentenceReorderer.particles(child);
            }
        }
    }

    public static void infinitives(Tree parse) {
        // do main logic of the rule here
        // rule will happen for every node in tree meeting criteria below
        SentenceReorderer.tree_flatten(parse);
        SentenceReorderer.reorder_verbs(parse);

    }

    public static void tree_flatten(Tree parse) {
        List<Tree> children = parse.getChildrenAsList();

        for (int i=0; i < children.size(); i++) {
            Tree child = children.get(i);

            // for every VP child
            if (child.label().value().equals("VP")) {
                // remove VP node and make note of its former location
                children.remove(i);
                int vp_loc = i;

                // go through all of VPs children
                List<Tree> vps_children = child.getChildrenAsList();
                for (Tree vps_child : vps_children) {
                    // add each child to the location the VP node was in, maintaining the relative order
                    children.add(vp_loc,vps_child);
                    vp_loc++;
                }
            }
        }

        parse.setChildren(children);


        // recursively call this on the whole tree to perform rule on all nodes
        if ( ! parse.isLeaf() ) {
            Tree[] children_recurse = parse.children();
            for (Tree child : children_recurse) {
                SentenceReorderer.tree_flatten(child);
            }
        }
    }

    public static void reorder_verbs(Tree parse) {
        if (parse.label().value().equals("S")) {
            List<Tree> children = parse.getChildrenAsList();

            // find the location of the VFIN node
            int vfin_loc = 0;
            for ( ; vfin_loc < children.size(); vfin_loc++) {
                Tree child = children.get(vfin_loc);
                if (child.label().value().startsWith("V") &&
                        child.label().value().endsWith("FIN")) {
                    break;
                }
            }

            int insertion_loc = vfin_loc + 1;
            for (int i = 0; i < children.size(); i++) {
                Tree child = children.get(i);
                if (child.label().value().startsWith("V") &&
                        ! child.label().value().endsWith("FIN")) {
                    SentenceReorderer.move_node(children,i,insertion_loc);
                    insertion_loc++;
                }
            }
            parse.setChildren(children);
        }


        // recursively call this on the whole tree to perform rule on all nodes
        if ( ! parse.isLeaf() ) {
            Tree[] children_recurse = parse.children();
            for (Tree child : children_recurse) {
                SentenceReorderer.reorder_verbs(child);
            }
        }
    }

    public static void negation(Tree parse) {
        // do main logic of the rule here
        // rule will happen for every node in tree meeting criteria below
        if (parse.label().value().equals("S")) {
            List<Tree> children = parse.getChildrenAsList();

            // find the location of the VFIN node
            int vfin_loc = 0;
            while ( vfin_loc < children.size()) {
                vfin_loc++;
                Tree child = children.get(vfin_loc);
                if (child.label().value().startsWith("V") &&
                        child.label().value().endsWith("FIN")) {
                    break;
                }
            }


            List<String> verb_tags = Arrays.asList("VVINF", "VVIMP", "VVIZU", "VVPP", "VAIMP", "VAINF", "VAPP", "VMINF", "VMPP");

            boolean has_vnonfin = false;
            for (Tree child : children) {
                if (verb_tags.contains(child.label().value())) {
                    has_vnonfin = true;
                    break;
                }
            }

            if (has_vnonfin) {
                for (int i = 0; i < children.size(); i++) {
                    Tree child = children.get(i);
                    if (child.label().value().equals("PTKNEG")) {
                        // move negpart to immediately after vfin
                        SentenceReorderer.move_node(children, i, vfin_loc + 1);
                        break;
                    }
                }
            }

            // if children contains (child.label == "VVFIN" or "VAFIN" or "VMFIN") = Tree vfin
            // AND (child.label == "VVINF" or VVIMP VVIZU VVPP VAIMP VAINF VAPP VMINF VMPP) = Tree vnonfin
            // AND (child.label == "PTKENEG") = negpart


            parse.setChildren(children);
        }

        if ( ! parse.isLeaf() ) {
            Tree[] children = parse.children();
            for (Tree child : children) {
                SentenceReorderer.negation(child);
            }
        }
    }

    public static void move_node(List<Tree> children, int from, int to) {
        children.add(to, children.get(from));
        if (to < from) {
            children.remove( from + 1 );
        } else {
            children.remove( from );
        }
    }

    public static int find_tree_head(Tree tree) {
        // Tree head = first(leftmost) child in children where child.label startswith V  (java doesnt have a nice way to do this?!)
        // assuming for now there will be only 1 V tag anyways
        // or if there are many we care about the leftmost only
        List<Tree> children = tree.getChildrenAsList();
        for ( int head_loc = 0; head_loc < children.size(); head_loc++ ) {
            Tree head = children.get(head_loc);

            // if we have found our tree head
            if (head.label().value().startsWith("V") &&
                    head.isPreTerminal()) {
                return head_loc;
            }
        }
        return -1;
    }

}