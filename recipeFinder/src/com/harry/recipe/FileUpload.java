package com.harry.recipe;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

public class FileUpload extends HttpServlet { 

	private ServletConfig config = null;
	
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		// TODO Auto-generated method stub
		this.config = config;
	}

	@Override
	public void destroy() {
		config = null;
		super.destroy();
	}

	public void doPost(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {
		int id = -1;
		
		Map<String, Ingredient> ingredients = null;
		Set<Recipe> recipes = null;
		Recipe recipe = null;
		List<Ingredient> ingres = null;
		Iterator<Ingredient> it = null;
		Ingredient ingredient = null;
		int count = 1;
		String fieldName = null;
		
		
//		String uploadPath = config.getInitParameter("uploadPath");
		String tempPath = config.getInitParameter("tempPath");
		res.setContentType("text/html; charset=GB2312");
		PrintWriter out = res.getWriter();
		DiskFileItemFactory factory = new DiskFileItemFactory();
		// maximum size that will be stored in memory
		factory.setSizeThreshold(4096);
		// the location for saving data that is larger than getSizeThreshold()
		factory.setRepository(new File(tempPath));

		ServletFileUpload upload = new ServletFileUpload(factory);
		// maximum size before a FileUploadException will be thrown
		upload.setSizeMax(1000000);
		try {
			List fileItems = upload.parseRequest(req);
			// assume we know there are two files. The first file is a small
			// text file, the second is unknown and is written to a file on
			// the server
			Iterator iter = fileItems.iterator();
			
			InputStream content = null;

			String regExp = ".*\\\\{0,1}(.+)$";

			String[] errorType = { ".exe", ".com", ".cgi", ".jsp" };
			Pattern p = Pattern.compile(regExp);
			while (iter.hasNext()) {
				FileItem item = (FileItem) iter.next();
				
				if (!item.isFormField()) {
					String name = item.getName();
					System.out.println(name);
					long size = item.getSize();
					if ((name == null || name.trim().equals("")) && size == 0)
						continue;
					Matcher m = p.matcher(name);
					boolean result = m.find();
					fieldName = item.getFieldName();
					if (result) {
						for (int temp = 0; temp < errorType.length; temp++) {
							if (m.group(1).endsWith(errorType[temp])) {
								throw new IOException(name + ": wrong type");
							}
						}
						
						if(fieldName.equals("ingredients")) {
							ingredients = FileUtils.parseIngredients(item.getInputStream());
						} else {
							recipes = FileUtils.parseRecipes(item.get());
						}
							
					} else {
						throw new IOException("fail to upload");
					}
				}
			}
			
			recipe = FileUtils.getValidResult(ingredients, recipes);
			
			if(recipe != null) {
				ingres = recipe.getIngredients();
				it = ingres.iterator();
				
				//System.out.println(req.getSession().getServletContext().getRealPath("/"));
				out.println("Recipe Name:" + recipe.getName() + "<br/><br/>");
				while(it.hasNext()) {
					ingredient = it.next();
					out.println("Ingredient " + count++ + ":" + ingredient.getItem() + "<br/>");
					out.println("Amount:" + ingredient.getAmount() + "<br/>");
					out.println("Unit:" + ingredient.getUnit() + "<br/><br/>");
				}
			} else {
				out.println("Order Takeout");
			}
			
		} catch (IOException e) {
			out.println("System IO Error, please use the samples!");
		} catch (FileUploadException e) {
			out.println("The formats of files uploaded are wrong, please use the samples!");
		} catch (Exception e) {
			out.println("Other errors, please use the samples!");
		}
		
	}
}
