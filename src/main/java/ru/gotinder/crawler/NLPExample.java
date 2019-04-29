package ru.gotinder.crawler;

import edu.stanford.nlp.international.russian.process.RussianLemmatizationAnnotator;
import edu.stanford.nlp.international.russian.process.RussianMorphoAnnotator;
import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.io.RuntimeIOException;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.util.CoreMap;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Launcher for test pipeline.
 *
 * @author Ivan Shilin
 * @author Liubov Kovriguina
 */

@Slf4j
public class NLPExample {
    // https://github.com/MANASLU8/CoreNLP


    //java -cp CoreNLP/target/stanford-corenlp-3.9.2.jar -Xmx8g edu.stanford.nlp.international.russian.process.Launcher
    // -tagger tagger/russian-ud-pos.tagger
    // -taggerMF tagger/russian-ud-mf.tagger
    // -pLemmaDict tagger/dict.tsv
    // -parser parser/nndep.rus.modelMFWiki100HS400_80.txt.gz
    // -pText 5ca130bbd81896150000e567.txt
    // -pResults 5ca130bbd81896150000e567.conll -mf


    private final static String DEFAULT_PATH_RESULTS = "ru_example.conllu";
    private final static String DEFAULT_PATH_PARSER_MODEL =
            "/media/mark/DATAPART1/nlp/parser/nndep.rus.modelMFWiki100HS400_80.txt.gz";
    private final static String DEFAULT_PATH_TAGGER =
            "/media/mark/DATAPART1/nlp/tagger/russian-ud-pos.tagger";
    private final static String DEFAULT_PATH_MF_TAGGER =
            "/media/mark/DATAPART1/nlp/tagger/russian-ud-mf.tagger";
    private final static String DEFAULT_LEMMA_DICT = "/media/mark/DATAPART1/nlp/tagger/dict.tsv";
    private final static String DEFAULT_PATH_TEXT = "ru_example.txt";
    private final static boolean MF = true;

    private static StanfordCoreNLP buildPipeline() {
        String tagger = DEFAULT_PATH_TAGGER;
        String taggerMF = DEFAULT_PATH_MF_TAGGER;
        String parser = DEFAULT_PATH_PARSER_MODEL;
        String pResults = DEFAULT_PATH_RESULTS;
        String pLemmaDict = DEFAULT_LEMMA_DICT;

        boolean mf = MF;


        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        if (mf) {
            pipeline.addAnnotator(new RussianMorphoAnnotator(new MaxentTagger(taggerMF)));
        }
        pipeline.addAnnotator(new POSTaggerAnnotator(new MaxentTagger(tagger)));

        Properties propsParser = new Properties();
        propsParser.setProperty("model", parser);
        propsParser.setProperty("tagger.model", tagger);
        pipeline.addAnnotator(new DependencyParseAnnotator(propsParser));

        if (pLemmaDict == null) {
            pipeline.addAnnotator(new RussianLemmatizationAnnotator());
        } else {
            pipeline.addAnnotator(new RussianLemmatizationAnnotator(pLemmaDict));
        }
        return pipeline;
    }

    public static void main(String[] args) throws FileNotFoundException, IOException {
        CoreDocument docs = new CoreDocument("У меня есть дочка. У меня есть дочь. Люблю свою дочь. Люблю дочурку.");
        StanfordCoreNLP pipeline = buildPipeline();

        pipeline.annotate(docs);
        Annotation annotation = docs.annotation();

        log.info("{}", annotation);
        List<CoreLabel> coreLabels = annotation.get(CoreAnnotations.TokensAnnotation.class);
        for (CoreLabel cl : coreLabels) {
//            log.info("{} {} {} ",cl.lemma() , cl.ner(),cl.nerConfidence());
            log.info("{} {}", cl.word(), cl.lemma());
        }

    }

    public static void loadConllFile(String inFile, List<CoreMap> sents) {
        CoreLabelTokenFactory tf = new CoreLabelTokenFactory(false);

        BufferedReader reader = null;
        try {
            reader = IOUtils.readerFromString(inFile);

            List<CoreLabel> sentenceTokens = new ArrayList<>();

            for (String line : IOUtils.getLineIterable(reader, false)) {
                String[] splits = line.split("\t");
                if (splits.length < 10) {
                    if (sentenceTokens.size() > 0) {
                        CoreMap sentence = new CoreLabel();
                        sentence.set(CoreAnnotations.TokensAnnotation.class, sentenceTokens);
                        sents.add(sentence);
                        sentenceTokens = new ArrayList<>();
                    }
                } else {
                    String word = splits[1], pos = splits[3], depType = splits[7];

                    int head = -1;
                    try {
                        head = Integer.parseInt(splits[6]);
                    } catch (NumberFormatException e) {
                        continue;
                    }

                    CoreLabel token = tf.makeToken(word, 0, 0);
                    token.setTag(pos);
                    token.set(CoreAnnotations.CoNLLDepParentIndexAnnotation.class, head);
                    token.set(CoreAnnotations.CoNLLDepTypeAnnotation.class, depType);
                    sentenceTokens.add(token);
                }
            }
        } catch (IOException e) {
            throw new RuntimeIOException(e);
        } finally {
            IOUtils.closeIgnoringExceptions(reader);
        }
    }

    public static void loadConllFileWithoutAnnotation(String inFile, List<CoreMap> sents) {
        CoreLabelTokenFactory tf = new CoreLabelTokenFactory(false);

        BufferedReader reader = null;
        try {
            reader = IOUtils.readerFromString(inFile);

            List<CoreLabel> sentenceTokens = new ArrayList<>();

            for (String line : IOUtils.getLineIterable(reader, false)) {
                String[] splits = line.split("\t");
                if (splits.length < 10) {
                    if (sentenceTokens.size() > 0) {
                        CoreMap sentence = new CoreLabel();
                        sentence.set(CoreAnnotations.TokensAnnotation.class, sentenceTokens);
                        sents.add(sentence);
                        sentenceTokens = new ArrayList<>();
                    }
                } else {
                    String word = splits[1];
                    CoreLabel token = tf.makeToken(word, 0, 0);
                    sentenceTokens.add(token);
                }
            }
        } catch (IOException e) {
            throw new RuntimeIOException(e);
        } finally {
            IOUtils.closeIgnoringExceptions(reader);
        }
    }

}
