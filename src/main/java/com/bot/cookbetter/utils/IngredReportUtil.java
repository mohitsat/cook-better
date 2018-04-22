package com.bot.cookbetter.utils;

import org.springframework.util.StringUtils;

import java.util.*;

public class IngredReportUtil {

    private IngredientNetwork network = null;
    private static final double NEG_INGRED_THRESHOLD = -0.15017;
    private static final double BEST_COMBINATION_THRESHOLD = 0.0923;

    Map<String, List<String>> negateCombinations;
    Map<String, List<String>> notRecommendedCombinations;
    Map<String, List<String>> bestCombinations;

    Map<String, Integer> recommendedIngred;

    String notRecommended;

    public void buildCombinations(String ingreds) {
        HashMap<String, Double> ingredPMI = null;
        this.negateCombinations = new HashMap<>();
        this.notRecommendedCombinations = new HashMap<>();
        this.bestCombinations = new HashMap<>();

        network = new IngredientNetwork(ingreds);
        ingredPMI = network.getIngredPMI();
        ingreds = removeCommonIngred(network.getCommonIngred(), ingreds, network.getNoSuchIngredient());
        for (String ingredSet : ingredPMI.keySet()) {
            double value = ingredPMI.get(ingredSet);
            String[] ingred = ingredSet.split(",");
            if (value < NEG_INGRED_THRESHOLD) {
                this.negateCombinations = updateIngredMap(this.negateCombinations, ingred[0], ingred[1]);
            } else if (value > BEST_COMBINATION_THRESHOLD) {
                this.bestCombinations = updateIngredMap(this.bestCombinations, ingred[0], ingred[1]);
            } else {
                this.notRecommendedCombinations = updateIngredMap(this.notRecommendedCombinations, ingred[0], ingred[1]);
            }
        }

        excludeIngredient(ingreds);
    }

    private String removeCommonIngred(Set<String> commonIngred, String ingreds, Set<String> noIngred) {
        String filteredIngreds = "";
        if(ingreds != null && ingreds.trim() != "" && commonIngred != null && !commonIngred.isEmpty()) {
            String[] ingred = ingreds.split(",");
            for (String str : ingred) {
                if (!commonIngred.contains(str.toLowerCase().trim()) && !noIngred.contains(str.toLowerCase().trim())) {
                    filteredIngreds = filteredIngreds + str + ",";
                }
            }
            int index = filteredIngreds.lastIndexOf(",");
            filteredIngreds = filteredIngreds.substring(0, index);
        }
        return filteredIngreds;
    }

    private Map<String, List<String>> updateIngredMap(Map<String, List<String>> ingredPMI, String ingred1, String ingred2) {
        updateList(ingredPMI, ingred1, ingred2);
        updateList(ingredPMI, ingred2, ingred1);
        return ingredPMI;
    }

    private void updateList(Map<String, List<String>> ingredPMI, String key, String value) {
        List<String> list;
        if (ingredPMI.containsKey(key)) {
            list = ingredPMI.get(key);
        } else {
            list = new ArrayList<>();
        }
        list.add(value);
        ingredPMI.put(key, list);
    }

    private String excludeIngredient(String ingreds) {

        String max_key = "";
        double count = ingreds.split(",").length * 0.5;

        for (String key : negateCombinations.keySet()) {
            int temp = negateCombinations.get(key).size();
            if (temp > count) {
                max_key = max_key + key + " ";
                count = temp;
            }
        }
        if (!(max_key.equals(null) || max_key.equals(""))) {
            String[] keys = max_key.split(" ");
            notRecommended = "We recommend to exclude following ingredients: \n";
            for (String key : keys) {
                if (negateCombinations.containsKey(key)) {
                    notRecommended = notRecommended + key + " as this doesn't taste good with ";
                    for (String st : negateCombinations.get(key)) {
                        notRecommended = notRecommended + st + ", ";
                    }
                    int index = notRecommended.lastIndexOf(",");
                    notRecommended = notRecommended.substring(0, index);
                    notRecommended = notRecommended + ".\n";
                }
            }
            return notRecommended;
        }
        return "No outlier found";
    }

    private void findRecommendedIngred(){

    }
}