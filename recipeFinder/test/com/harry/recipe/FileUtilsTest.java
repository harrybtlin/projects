package com.harry.recipe;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

public class FileUtilsTest {
	
	private static SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
	
	private void testIngredient(Ingredient ingredient, String itemName, int amount, Unit unit, Date useBy) {
		assertEquals(itemName, ingredient.getItem());
		assertEquals(amount, ingredient.getAmount());
		assertEquals(Unit.slices, ingredient.getUnit());
		
		if(useBy != null)
			assertEquals(useBy, ingredient.getUseBy());
	}
	
	private void testIngredient(Ingredient ingredient, String itemName, int amount, Unit unit) {
		testIngredient(ingredient, itemName, amount, unit, null);
	}
	
	@Test
	public void testParseRecipes() {
		String test = "[{\"name\":\"grilled cheese on toast\", \"ingredients\":[{\"item\":\"bread\", \"amount\":\"2\", \"unit\":\"slices\"}, {\"item\":\"cheese\", \"amount\":\"2\", \"unit\":\"slices\"}]}]";
		Set<Recipe> recipes = FileUtils.parseRecipes(test.getBytes());
		Recipe recipe = null;
		Iterator<Recipe> it = recipes.iterator();
		List<Ingredient> ingredients = null;
		Ingredient ingredient = null;
		while(it.hasNext()) {
			recipe = it.next();
			assertEquals("grilled cheese on toast", recipe.getName());
			
			ingredients = recipe.getIngredients();
			
			ingredient = ingredients.get(0);
			this.testIngredient(ingredient, "bread", 2, Unit.slices);
			
			ingredient = ingredients.get(1);
			this.testIngredient(ingredient, "cheese", 2, Unit.slices);
		}
	}
	
	@Test
	public void testParseIngredients() {
		String test = "bread, 10, slices, 10/12/2015\ncheese, 10, slices, 8/5/2015";
		byte[] tb = test.getBytes();
		
		InputStream bais = new ByteArrayInputStream(tb);
		
		Map<String, Ingredient> ingredientsMap = FileUtils.parseIngredients(bais);
		
		Date date = null;
		try {
			date = sdf.parse("10/12/2015");
			Ingredient ingredient = ingredientsMap.get("bread");
			testIngredient(ingredient, "bread", 10, Unit.slices, date);
			
			date = sdf.parse("8/5/2015");
			ingredient = ingredientsMap.get("cheese");
			testIngredient(ingredient, "cheese", 10, Unit.slices, date);
			
		} catch (ParseException e1) {
			fail("fail");
		}
		
		
		try {
			if(bais != null)
				bais.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void testGetValidResult() {
		String recipes = "[{\"name\":\"grilled cheese on toast\", \"ingredients\":[{\"item\":\"bread\", \"amount\":\"2\", \"unit\":\"slices\"}, {\"item\":\"cheese\", \"amount\":\"2\", \"unit\":\"slices\"}]}, {\"name\":\"cofo\", \"ingredients\":[{\"item\":\"mixed salad\", \"amount\":\"150\", \"unit\":\"grams\"}]}]";
		String ingredients = "bread, 10, slices, 10/12/2015\ncheese, 10, slices, 8/5/2015\nbutter, 250, grams, 10/5/2015\npeanut butter, 250, grams, 2/12/2015\nmixed salad, 150, grams, 29/4/2015";
		
		byte[] tb = ingredients.getBytes();
		
		InputStream bais = new ByteArrayInputStream(tb);
		Recipe recipe = FileUtils.getValidResult(FileUtils.parseIngredients(bais), FileUtils.parseRecipes(recipes.getBytes()));
		
		assertEquals(recipe.getName(), "grilled cheese on toast");
		
		try {
			if(bais != null)
				bais.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
