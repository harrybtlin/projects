package com.harry.recipe;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class FileUtils {
	
	private static SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
	
	private static Gson gson = new Gson();
	
	public static Set<Recipe> parseRecipes(byte[] in) {
		if(in == null) {
			return new HashSet<Recipe>();
		}
		
		String content = new String(in);
		Type type = new TypeToken<Set<Recipe>>(){}.getType();
		return gson.fromJson(content, type);
	}
	
	public static Map<String, Ingredient> parseIngredients(InputStream in) {
		
		BufferedReader is = new BufferedReader(new InputStreamReader(in));
		String row = null;
		String[] cells = null;
		Ingredient ingredient = null;
		Map<String, Ingredient> ingredients = new HashMap<String, Ingredient>();
		Date now = new Date();
		Date useBy = null;
		
		if(in == null) {
			return ingredients;
		}
		
		try {
			while((row = is.readLine()) != null) {
				cells = row.split(",");
				if(cells != null && cells.length == 4) {
					ingredient = parseIngredient(cells[0].trim(), cells[1].trim(), cells[2].trim(), cells[3].trim());
					
					useBy = ingredient.getUseBy();
					
					if(useBy != null) {
						if(now.before(useBy) || now.equals(useBy)) {
							ingredients.put(ingredient.getItem(), ingredient);
						}
					}
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return ingredients;
	}
	
	private static Ingredient parseIngredient(String item, String amount, String unit, String useBy) throws ParseException {
		int am = Integer.parseInt(amount);
		Date useDate = sdf.parse(useBy);
		Unit un = Unit.valueOf(unit);
		
		Ingredient ingredient = new Ingredient(item, am, un, useDate);
		return ingredient;
	}
	
	private static boolean isIngredientsValid(Map<String, Ingredient> ingredientsMap, List<Ingredient> ingredients) {
		
		if(ingredients == null) {
			return false;
		}
		
		Iterator<Ingredient> it = ingredients.iterator();
		Ingredient ingredient = null;
		Ingredient ingredientInMap = null;
		int amount = 0;
		Unit unit = null;
		
		while(it.hasNext()) {
			ingredient = it.next();
			
			if(ingredient != null) {
				ingredientInMap = ingredientsMap.get(ingredient.getItem());
				
				if(ingredientInMap == null) {
					return false;
				} else {
					if(ingredientInMap.getAmount() < ingredient.getAmount() || ingredientInMap.getUnit() != ingredient.getUnit()) {
						return false;
					}
				}
			} else {
				return false;
			}
		}
		
		return true;
	}
	
	private static void sortRecipeIngredientsByDate(final Map<String, Ingredient> ingredientsMap, List<Ingredient> ingredients) {
		Collections.sort(ingredients, new Comparator<Ingredient>() {
			
			@Override
			public int compare(Ingredient o1, Ingredient o2) {
				
				Date date1 = null;
				
				Date date2 = null;
				
				if(o1 != null && o2 != null) {
					date1 = ingredientsMap.get(o1.getItem()).getUseBy();
					date2 = ingredientsMap.get(o2.getItem()).getUseBy();
					
					if(date1 != null && date2 != null) {
						if(date1.before(date2)) {
							return -1;
						} else if(date1.after(date2)) {
							return 1;
						} else {
							return 0;
						}
					} else {
						return 0;
					}
				} else {
					return 0;
				}
			}
			
		});
	}
	
	private static Recipe compareTwoRecipes(Map<String, Ingredient> ingredientsMap, Recipe r1, Recipe r2) {
		
		List<Ingredient> ingredients1 = r1.getIngredients();
		
		int size1 = ingredients1.size();
		
		Date date1 = null;
		
		List<Ingredient> ingredients2 = r2.getIngredients();
		
		int size2 = ingredients2.size();
		
		Date date2 = null;
		
		int size = size1 > size2 ? size2 : size1;
		
		for(int i=0; i<size; i++) {
			
			date1 = ingredientsMap.get(ingredients1.get(i).getItem()).getUseBy();
			
			date2 = ingredientsMap.get(ingredients2.get(i).getItem()).getUseBy();
			
			if(date1.before(date2)) {
				return r1;
			} else if(date1.after(date2)) {
				return r2;
			} else {
				
			}
		}
		
		if(size1 >= size2) {
			return r1;
		} else {
			return r2;
		}
		 
	}
	
	private static Recipe chooseOneFromRecipes(Map<String, Ingredient> ingredientsMap, List<Recipe> recipes) {
		int size = recipes.size();
		
		Recipe recipe = null;
		
		if(size == 1) {
			return recipes.get(0);
		}
		
		for(int i=0; i<size-1; i++) {
			
			if(recipe == null) {
				recipe = recipes.get(i);
			}
			recipe = compareTwoRecipes(ingredientsMap, recipe, recipes.get(i+1));
		}
		
		return recipe;
		
	}
	
	public static Recipe getValidResult(Map<String, Ingredient> ingredientsMap, Set<Recipe> recipes) {
		Iterator<Recipe> it = recipes.iterator();
		Recipe recipe = null;
		List<Ingredient> ingreds = null;
		
		List<Recipe> validRecipes = new ArrayList<Recipe>();
		while(it.hasNext()) {
			recipe = it.next();
			ingreds = recipe.getIngredients();
			
			if(isIngredientsValid(ingredientsMap, ingreds)) {
				sortRecipeIngredientsByDate(ingredientsMap, ingreds);
				validRecipes.add(recipe);
			}
		}
		
		return chooseOneFromRecipes(ingredientsMap, validRecipes);
	}
	
//	public static void main(String[] args) {
//		String test = "[{\"name\":\"grilled cheese on toast\", \"ingredients\":[{\"item\":\"bread\", \"amount\":\"2\", \"unit\":\"slices\"}, {\"item\":\"cheese\", \"amount\":\"2\", \"unit\":\"slices\"}]}, {\"name\":\"salad sandwich\", \"ingredients\":[{\"item\":\"bread\", \"amount\":\"2\", \"unit\":\"slices\"}, {\"item\":\"mixed salad\", \"amount\":\"100\", \"unit\":\"grams\"}]}]";
//		
//		Set<Recipe> set = parseRecipes(test.getBytes());
//		
//		Iterator<Recipe> it = set.iterator();
//		
//		Recipe recipe = null;
//		Ingredient ingredient = null;
//		while(it.hasNext()) {
//			recipe = it.next();
//			ingredient = recipe.getIngredients().get(1);
//			
//			System.out.println(ingredient.getUnit());
//		}
//		
//	}
}
