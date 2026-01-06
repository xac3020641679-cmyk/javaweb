package com.enterprise.catering.controller;

import com.enterprise.catering.dao.RecipeDAO;
import com.enterprise.catering.model.Recipe;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 食谱管理接口
 * 路径前缀：/api/recipes
 */
@RestController
@RequestMapping("/api/recipes")
public class RecipeController {

    @Autowired
    private RecipeDAO recipeDAO;

    @GetMapping
    public ResponseEntity<List<Recipe>> list() {
        try {
            List<Recipe> recipes = recipeDAO.getAllRecipes();
            return ResponseEntity.ok(recipes);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(java.util.Collections.emptyList());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Recipe> get(@PathVariable int id) {
        try {
            Recipe recipe = recipeDAO.getRecipeById(id);
            if (recipe == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(recipe);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Recipe recipe) {
        try {
            recipeDAO.addRecipe(recipe);
            Map<String, Object> resp = new HashMap<>();
            resp.put("message", "created");
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> resp = new HashMap<>();
            resp.put("message", "创建失败: " + e.getMessage());
            return ResponseEntity.status(500).body(resp);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable int id, @RequestBody Recipe recipe) {
        try {
            recipe.setId(id);
            recipeDAO.updateRecipe(recipe);
            Map<String, Object> resp = new HashMap<>();
            resp.put("message", "updated");
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> resp = new HashMap<>();
            resp.put("message", "更新失败: " + e.getMessage());
            return ResponseEntity.status(500).body(resp);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable int id) {
        try {
            recipeDAO.deleteRecipe(id);
            Map<String, Object> resp = new HashMap<>();
            resp.put("message", "deleted");
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> resp = new HashMap<>();
            resp.put("message", "删除失败: " + e.getMessage());
            return ResponseEntity.status(500).body(resp);
        }
    }
}


