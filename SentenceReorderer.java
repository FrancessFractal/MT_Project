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

    }

    public static void subject(Tree parse) {

    }

    public static void particles(Tree parse) {

    }

    public static void infinitives(Tree parse) {

    }

    public static void negation(Tree parse) {

    }

}
