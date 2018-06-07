package edu.kennesaw.cs.core;

import edu.kennesaw.cs.readers.Document;
import java.lang.Math;
import edu.kennesaw.cs.readers.ReadCranfieldData;
import opennlp.tools.stemmer.PorterStemmer;
import opennlp.tools.lemmatizer.DictionaryLemmatizer;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.langdetect.*;
import java.io.*;



import java.util.*;

/*
This class is an example implementation of the CoreSearch, you can either modify or write another implementation of the Core Search.
 */

/**
 * Created by Ferosh Jacob
 * Date: 01/27/18
 * KSU: CS 7263 Text Mining
 */
public class CoreSearchImpl implements CoreSearch {


    Map<String, List<Integer>> invertedIndex = new HashMap<String, List<Integer>>();
    List<tfidfAttr> docTfidf = new ArrayList<tfidfAttr>();
    List<List<String>> documents = new ArrayList<List<String>>();


    public void init() {

    }

    public class tfidfAttr{
        String token;
        int docId;
        Double weight;
        tfidfAttr(String token, int docId, Double weight){
            this.token = token;
            this.docId = docId;
            this.weight = weight;
        }
    }

    public static void removeDuplicateWithOrder(List list) {
        Set set = new HashSet();
        List newList = new ArrayList();
        for (Iterator iter = list.iterator(); iter.hasNext();) {
            Object element = iter.next();
            if (set.add(element))
                newList.add(element);
        }
        list.clear();
        list.addAll(newList);
    }


    /*
    A very simple tokenization.
     */
    public String[] tokenize(String title) {
        return title.split(" ");
    }

//    public String stemming(String token){
//        PorterStemmer PS = new PorterStemmer();
//        char temp [] = new char[token.length()];
//        temp = token.toCharArray();
//        PS.add(temp, token.length());
//        PS.stem();
//        return  PS.toString();
//    }

    public String porterStemmer(String token){
        opennlp.tools.stemmer.PorterStemmer PS = new opennlp.tools.stemmer.PorterStemmer();
        char[] temp = token.toCharArray();
        for (char ch :temp) PS.add(ch);
        PS.stem();
        return PS.toString();
    }

    public String normalization(String token){
        Normalization NL = new Normalization();
        token = NL.normalization(token);
        return token;
    }

    public void addToIndex(Document document) {
        RemoveStopWords RSW = new RemoveStopWords();
        String[] tokens = tokenize((new StringBuilder()).append(document.getTitle()).append(document.getBody()).toString());
        tokens = RSW.remove(tokens);
        for (int i = 0; i < tokens.length; i++){
            tokens[i] = tokens[i].toLowerCase();
            tokens[i] = normalization(tokens[i]);
            tokens[i] = porterStemmer(tokens[i]);
        }

        List<String> tokensList = new ArrayList<String>(Arrays.asList(tokens));

        tokens = tokensList.toArray(new String[0]);

        for (String token : tokens) {
            token = token.toLowerCase();
            addTokenToIndex(token, document.getId());
        }

        documents.add(tokensList);
    }

    private void addTokenToIndex(String token, int docId) {

        if (invertedIndex.containsKey(token)) { //check hashmap constant token
            List<Integer> docIds = invertedIndex.get(token); // get given value in token
            docIds.add(docId); //add id to given key
            removeDuplicateWithOrder(docIds);
            Collections.sort(docIds); //sort docID
            invertedIndex.put(token, docIds); //put back to hashmap
        } else {
            List<Integer> docIds = new ArrayList<Integer>();
            docIds.add(docId);
            invertedIndex.put(token, docIds);
        }
    }


    /*
    A very simple search implementation.
     */
    public List<Integer> search(String query) {
        RemoveStopWords RSW = new RemoveStopWords();
        String[] queryTokens = tokenize(query);
        queryTokens = RSW.remove(queryTokens);
        for (int i = 0; i < queryTokens.length; i++) {
            queryTokens[i] = queryTokens[i].toLowerCase();
            queryTokens[i] = normalization(queryTokens[i]);
            queryTokens[i] = porterStemmer(queryTokens[i]);
            //System.out.println(queryTokens[i]);
        }
        queryTokens = removeNotIndexTokens(queryTokens);

        //List<String> queryTokensList = new ArrayList<String>(Arrays.asList(queryTokens));

        List<Integer> rank = new ArrayList<Integer>();

        TFIDFCalculator calculateTFIDF = new TFIDFCalculator();
        Double weight = 0.0;
        HashMap<Integer, Double> TFIDF = new HashMap<Integer, Double>();
//        for (int i = 0; i < documents.size(); i++){
//            for (int j = 0; j < queryTokensList.size(); j++){
//                weight += calculateTFIDF.tfIdf(documents.get(i), documents, queryTokensList.get(j));
//            }
//            if (weight != 0.0 && !weight.isNaN())
//                TFIDF.put(i, weight);
//            weight = 0.0;
//        }

//        for (int i = 0; i < documents.size(); i++){
//            for (int j = 0; j < docTfidf.size(); j++){
//                for (String token : queryTokens){
//                    int docId = docTfidf.get(j).docId;
//                    String docToken = docTfidf.get(j).token;
//                    if (docId == i){
//                        if (docToken.equals(token)){
//                            Double docWeight = docTfidf.get(j).weight;
//                            weight += docWeight;
//                        }
//                    }
//                }
//                if (weight != 0.0){
//                    TFIDF.put(i+1, weight);
//                }
//                weight = 0.0;
//            }
//        }

        for (int i = 0; i < documents.size(); i++){
            for (int j = 0; j < documents.get(i).size(); j++){
                for (String token : queryTokens){
                    if (token.equals(documents.get(i).get(j))){
                        weight += calculateTFIDF.tfIdf(documents.get(i), documents, token);
                        break;
                    }
                }
            }
            TFIDF.put(i+1, weight);
            weight = 0.0;
        }

        TFIDF = sortByValues(TFIDF);

        for (int tfidf : TFIDF.keySet()){
            rank.add(tfidf);
        }

        return rank;

//        List<Integer> mergedDocIds = new ArrayList<Integer>();
//        if (queryTokens.length == 0) return mergedDocIds;
//        int index = 1;
//        if (queryTokens.length == 1)
//            invertedIndex.get(queryTokens[0]); //get given key's value
//
//        List<Integer> initial = invertedIndex.get(queryTokens[0]);
//        //System.out.println(invertedIndex.get(queryTokens[0]));
//        while (index < queryTokens.length) {
//            initial = mergeTwoDocIds(initial, invertedIndex.get(queryTokens[index]));
//            index++;
//        }
//        removeDuplicateWithOrder(initial);
//        return initial;
    }

    private static HashMap sortByValues(HashMap map) {
        List list = new LinkedList(map.entrySet());
        // Defined Custom Comparator here
        Collections.sort(list, new Comparator() {
            public int compare(Object o1, Object o2) {
                return ((Comparable) ((Map.Entry) (o2)).getValue())
                        .compareTo(((Map.Entry) (o1)).getValue());
            }
        });
        //Collections.reverse(list);

        // Here I am copying the sorted list in HashMap
        // using LinkedHashMap to preserve the insertion order
        HashMap sortedHashMap = new LinkedHashMap();
        for (Iterator it = list.iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry) it.next();
            sortedHashMap.put(entry.getKey(), entry.getValue());
        }
        return sortedHashMap;
    }

    /*
    Ignore terms in query that are not in Index
     */
    private String[] removeNotIndexTokens(String[] split) {
        List<String> indexedTokens = new ArrayList<String>();
        for (String token : split) {
            if (invertedIndex.containsKey(token)) indexedTokens.add(token);
        }
        return indexedTokens.toArray(new String[indexedTokens.size()]);
    }


    /*
    AND Merging postings!!
     */
    public List<Integer> mergeTwoDocIds(List<Integer> docList1, List<Integer> docList2) {
        int docIndex1 = 0;
        int docIndex2 = 0;
        List<Integer> mergedList = new ArrayList<Integer>();
        while (docIndex1 < docList1.size() && docIndex2 < docList2.size()) {
            if (docList1.get(docIndex1).intValue() == docList2.get(docIndex2).intValue()) {
                mergedList.add(docList1.get(docIndex1));
                docIndex1++;
                docIndex2++;
            } else if (docList1.get(docIndex1) < docList2.get(docIndex2)) {
                mergedList.add(docList1.get(docIndex1));
                docIndex1++;
            } else if (docList1.get(docIndex1) > docList2.get(docIndex2)) {
                mergedList.add(docList1.get(docIndex1));
                docIndex2++;
            }
        }

        return mergedList;
    }
}
