package com.enterprise.catering.controller;

import com.enterprise.catering.dao.MenuDAO;
import com.enterprise.catering.dao.MenuItemDAO;
import com.enterprise.catering.dao.RecipeDAO;
import com.enterprise.catering.model.Menu;
import com.enterprise.catering.model.MenuItem;
import com.enterprise.catering.model.Recipe;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 菜单相关 REST 接口：
 * - /api/menus        : 管理员菜单列表、创建、更新、删除
 * - /api/menus/today  : 员工“今日菜单”（包含菜单项）
 * - /api/menus/{id}   : 菜单详情（包含菜单项）
 */
@RestController
@RequestMapping("/api/menus")
public class MenuController {

    @Autowired
    private MenuDAO menuDAO;
    @Autowired
    private MenuItemDAO menuItemDAO;
    @Autowired
    private RecipeDAO recipeDAO;

    @GetMapping
    public ResponseEntity<?> listMenus() {
        List<Menu> menus = menuDAO.getAllMenus();
        return ResponseEntity.ok(menus);
    }

    @GetMapping("/today")
    public ResponseEntity<?> todayMenus() {
        List<Menu> activeMenus = menuDAO.getActiveMenusForToday();
        // 组装菜单及其菜单项
        Map<Integer, List<MenuItem>> itemsMap = new HashMap<>();
        for (Menu menu : activeMenus) {
            itemsMap.put(menu.getId(), menuItemDAO.getMenuItemsByMenuId(menu.getId()));
        }
        Map<String, Object> result = new HashMap<>();
        result.put("menus", activeMenus);
        result.put("itemsMap", itemsMap);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getMenu(@PathVariable int id) {
        Menu menu = menuDAO.getMenuById(id);
        if (menu == null) {
            return ResponseEntity.notFound().build();
        }
        List<MenuItem> items = menuItemDAO.getMenuItemsByMenuId(id);
        Map<String, Object> result = new HashMap<>();
        result.put("menu", menu);
        result.put("items", items);
        return ResponseEntity.ok(result);
    }

    @PostMapping
    public ResponseEntity<?> createMenu(@RequestBody MenuRequest body) throws ParseException {
        Menu menu = new Menu();
        menu.setName(body.getName());
        menu.setDate(parseDate(body.getDate()));
        menu.setActive(body.isActive());
        int menuId = menuDAO.addMenu(menu);
        Map<String, Object> result = new HashMap<>();
        result.put("id", menuId);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateMenu(@PathVariable int id, @RequestBody MenuRequest body) throws ParseException {
        Menu menu = new Menu();
        menu.setId(id);
        menu.setName(body.getName());
        menu.setDate(parseDate(body.getDate()));
        menu.setActive(body.isActive());
        menuDAO.updateMenu(menu);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMenu(@PathVariable int id) {
        menuItemDAO.deleteMenuItemsByMenuId(id);
        menuDAO.deleteMenu(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/items")
    public ResponseEntity<?> getMenuItems(@PathVariable int id) {
        List<MenuItem> items = menuItemDAO.getMenuItemsByMenuId(id);
        return ResponseEntity.ok(items);
    }

    @PostMapping("/{id}/items")
    public ResponseEntity<?> addMenuItem(@PathVariable int id, @RequestBody MenuItemRequest body) {
        int recipeId = body.getRecipeId();
        String name = body.getName();
        String unit = body.getUnit();
        double price = body.getPrice();
        String image = body.getImage();

        Recipe recipe = null;
        if (recipeId > 0) {
            recipe = recipeDAO.getRecipeById(recipeId);
        }
        if (recipe != null) {
            if (name == null || name.isEmpty()) {
                name = recipe.getName();
            }
            if (unit == null || unit.isEmpty()) {
                unit = recipe.getUnit();
            }
            if (image == null || image.isEmpty()) {
                image = recipe.getImage();
            }
            // 仅在价格为负数时才使用食谱价格；允许 0 价格的菜品
            if (price < 0) {
                price = recipe.getPrice();
            }
        }

        // 验证必填字段
        if (name == null || name.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "菜名不能为空"));
        }
        if (unit == null || unit.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "单位不能为空"));
        }
        // 允许 0 价格（免费菜品），仅禁止负数
        if (price < 0) {
            return ResponseEntity.badRequest().body(Map.of("message", "单价不能为负数"));
        }

        MenuItem item = new MenuItem();
        item.setMenuId(id);
        item.setRecipeId(recipeId);
        item.setName(name.trim());
        item.setUnit(unit.trim());
        item.setPrice(price);
        item.setImage(image != null ? image.trim() : "");

        try {
            menuItemDAO.addMenuItem(item);
            // 返回成功响应，包含保存的菜品信息
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "菜品添加成功");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("message", "保存菜品失败: " + e.getMessage()));
        }
    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<?> updateMenuItem(@PathVariable int itemId, @RequestBody MenuItemRequest body) {
        MenuItem existing = menuItemDAO.getMenuItemById(itemId);
        if (existing == null) {
            return ResponseEntity.notFound().build();
        }

        int recipeId = body.getRecipeId();
        String name = body.getName();
        String unit = body.getUnit();
        double price = body.getPrice();
        String image = body.getImage();

        if (image == null || image.isEmpty()) {
            image = existing.getImage();
        }
        if (image == null || image.isEmpty() && recipeId > 0) {
            Recipe recipe = recipeDAO.getRecipeById(recipeId);
            if (recipe != null && recipe.getImage() != null) {
                image = recipe.getImage();
            }
        }

        existing.setRecipeId(recipeId);
        if (name != null && !name.isEmpty()) existing.setName(name);
        if (unit != null && !unit.isEmpty()) existing.setUnit(unit);
        if (price > 0) existing.setPrice(price);
        existing.setImage(image);

        menuItemDAO.updateMenuItem(existing);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<?> deleteMenuItem(@PathVariable int itemId) {
        menuItemDAO.deleteMenuItem(itemId);
        return ResponseEntity.ok().build();
    }

    private Date parseDate(String s) throws ParseException {
        return new SimpleDateFormat("yyyy-MM-dd").parse(s);
    }

    // DTO
    public static class MenuRequest {
        private String name;
        private String date; // yyyy-MM-dd
        private boolean active;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public boolean isActive() {
            return active;
        }

        public void setActive(boolean active) {
            this.active = active;
        }
    }

    public static class MenuItemRequest {
        private int recipeId;
        private String name;
        private String image;
        private String unit;
        private double price;

        public int getRecipeId() {
            return recipeId;
        }

        public void setRecipeId(int recipeId) {
            this.recipeId = recipeId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getImage() {
            return image;
        }

        public void setImage(String image) {
            this.image = image;
        }

        public String getUnit() {
            return unit;
        }

        public void setUnit(String unit) {
            this.unit = unit;
        }

        public double getPrice() {
            return price;
        }

        public void setPrice(double price) {
            this.price = price;
        }
    }
}


