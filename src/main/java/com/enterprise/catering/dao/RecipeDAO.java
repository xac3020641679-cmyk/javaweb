package com.enterprise.catering.dao;

import com.enterprise.catering.model.Recipe;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class RecipeDAO {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final RowMapper<Recipe> RECIPE_ROW_MAPPER = new RowMapper<Recipe>() {
        @Override
        public Recipe mapRow(ResultSet rs, int rowNum) throws SQLException {
            Recipe recipe = new Recipe();
            recipe.setId(rs.getInt("id"));
            recipe.setName(rs.getString("name"));
            recipe.setCategory(rs.getString("category"));
            recipe.setImage(rs.getString("image"));
            recipe.setUnit(rs.getString("unit"));
            recipe.setPrice(rs.getDouble("price"));
            recipe.setCreatedAt(rs.getTimestamp("created_at"));
            return recipe;
        }
    };

    public List<Recipe> getAllRecipes() {
        String sql = "SELECT * FROM recipes";
        try {
            return jdbcTemplate.query(sql, RECIPE_ROW_MAPPER);
        } catch (Exception e) {
            e.printStackTrace();
            return java.util.Collections.emptyList();
        }
    }

    public Recipe getRecipeById(int id) {
        String sql = "SELECT * FROM recipes WHERE id = ?";
        try {
            List<Recipe> recipes = jdbcTemplate.query(sql, RECIPE_ROW_MAPPER, id);
            return recipes.isEmpty() ? null : recipes.get(0);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void addRecipe(Recipe recipe) {
        String sql = "INSERT INTO recipes (name, category, image, unit, price, created_at) VALUES (?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, 
            recipe.getName(),
            recipe.getCategory(),
            recipe.getImage(),
            recipe.getUnit(),
            recipe.getPrice(),
            new java.sql.Timestamp(recipe.getCreatedAt().getTime()));
    }

    public void updateRecipe(Recipe recipe) {
        String sql = "UPDATE recipes SET name = ?, category = ?, image = ?, unit = ?, price = ? WHERE id = ?";
        jdbcTemplate.update(sql, 
            recipe.getName(),
            recipe.getCategory(),
            recipe.getImage(),
            recipe.getUnit(),
            recipe.getPrice(),
            recipe.getId());
    }

    public void deleteRecipe(int id) {
        String sql = "DELETE FROM recipes WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }
}
