package MT_Project;

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
                List<Tree> leaves = parse.getLeaves();
                for ( Tree leaf : leaves ) {
                    out.write(leaf.label() + " ");
                }
                out.write("\n");

            }
            out.close();

        } catch (Exception e) {
            System.err.println("Something went wrong. " + e.getMessage());
        }


    }

    public static void verb_initial(Tree parse) {

        // do main logic of the rule here
        // rule will happen for every node in tree meeting criteria below
        if (parse.label().value().equals("VP")) {

            List<Tree> children = parse.getChildrenAsList();
            // TODO: manipulate children

            // Tree head = first(leftmost) child in children where child.label startswith V  (java doesnt have a nice way to do this?!)
            // assuming for now there will be only 1 V tag anyways
            // or if there are many we care about the leftmost only

            // insert head into first position in children array  (is there a way to do this without making a new List and slicing and shit?)

            parse.setChildren(children);
        }

        // recursively call this on the whole tree to perform rule on all VPs
        if ( ! parse.isLeaf() ) {
            Tree[] children = parse.children();
            for (Tree child : children) {
                SentenceReorderer.verb_initial(child);
            }
        }
    }

    public static void verb_second(Tree parse) {
        // do main logic of the rule here
        // rule will happen for every node in tree meeting criteria below

        // List<String> conjunction_tags = ["KOUS", "PWAV", "PWS", "PRELS", "PRELAT"]
        //if (parse.label().value().equals("S")) {
            // List<Tree> children = parse.getChildrenAsList();
            // if child in conjunction_tags {

                // do the thing!
                // Tree head = first(leftmost) child in children where child.label startswith V
                // assuming for now there will be only 1 V tag anyways
                // or if there are many we care about the leftmost only

                // insert head into second position in children ( or immediately after conj, whichever is easier to do)

            //parse.setChildren(children);
        //}

        // recursively call this on the whole tree to perform rule on all Ss
        if ( ! parse.isLeaf() ) {
            Tree[] children = parse.children();
            for (Tree child : children) {
                SentenceReorderer.verb_second(child);
            }
        }
    }

    public static void subject(Tree parse) {
        // do main logic of the rule here
        // rule will happen for every node in tree meeting criteria below
        if (parse.label().value().equals("S")) {

            List<Tree> children = parse.getChildrenAsList();
            // TODO: manipulate children

            // Tree head = first(leftmost) child in children where child.label startswith V  (this gets done 3 times, should be separate subroutine?)
            // assuming for now there will be only 1 V tag anyways
            // or if there are many we care about the leftmost only

            // Tree subject = leftmost child in children where child.label = [NP or PPER]

            // insert subject into children at position immediately before head
            parse.setChildren(children);
        }

        // recursively call this on the whole tree to perform rule on all Ss
        if ( ! parse.isLeaf() ) {
            Tree[] children = parse.children();
            for (Tree child : children) {
                SentenceReorderer.subject(child);
            }
        }

    }

    public static void particles(Tree parse) {
        // do main logic of the rule here
        // rule will happen for every node in tree meeting criteria below

        //if (parse.label().value().equals("S")) {
            // List<Tree> children = parse.getChildrenAsList();
            // if (child.label == "VVFIN" or "VAFIN" or "VMFIN") AND (child.label == "PTKVZ") in children  {

                // do the thing!
                // move the PTKVZ child to immediately before V*FIN

            //parse.setChildren(children);
        //}

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
// commented out because it causes an exception of some sort and halts the program.
//        parse = tree_flatten(parse);
        if (parse.label().value().equals("S")) {

            List<Tree> children = parse.getChildrenAsList();
            // TODO: manipulate children

                // if children contains (child.label == "VVFIN" or "VAFIN" or "VMFIN") = Tree vfin
                // AND (child.label == "VVINF" or VVIMP VVIZU VVPP VAIMP VAINF VAPP VMINF VMPP) = Tree vnonfin

                    // move vnonfin to immediately after vfin

            parse.setChildren(children);
        }

        // recursively call this on the whole tree to perform rule on all Ss
        if ( ! parse.isLeaf() ) {
            Tree[] children = parse.children();
            for (Tree child : children) {
                SentenceReorderer.infinitives(child);
            }
        }

    }

    public static void negation(Tree parse) {
        // do main logic of the rule here
        // rule will happen for every node in tree meeting criteria below
        if (parse.label().value().equals("S")) {

            List<Tree> children = parse.getChildrenAsList();
            // TODO: manipulate children

            // if children contains (child.label == "VVFIN" or "VAFIN" or "VMFIN") = Tree vfin
            // AND (child.label == "VVINF" or VVIMP VVIZU VVPP VAIMP VAINF VAPP VMINF VMPP) = Tree vnonfin
            // AND (child.label == "PTKENEG") = negpart

                // move negpart to immediately after vfin

            parse.setChildren(children);
        }

        if ( ! parse.isLeaf() ) {
            Tree[] children = parse.children();
            for (Tree child : children) {
                SentenceReorderer.negation(child);
            }
        }
    }


    public static Tree tree_flatten(Tree parse) {
        if (parse.label().value().equals("VP")) {
            List<Tree> children = parse.getChildrenAsList();
            // for child in children:
                // child.parent = parse.parent
            // remove the parse subtree from the sentence (delete this node or something)
        }
        return null;
    }
}