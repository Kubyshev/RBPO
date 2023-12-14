package com.spring_recipe.demo.api.rest;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.spring_recipe.demo.domain.dto.CreateRecipeRequest;
import com.spring_recipe.demo.domain.entity.Recipe;
import com.spring_recipe.demo.domain.dto.RecipeDto;
import com.spring_recipe.demo.domain.exceptions.RecipeAlreadyExistException;
import com.spring_recipe.demo.domain.exceptions.RecipeNotFoundException;
import com.spring_recipe.demo.service.UserDetailsServiceImpl;
import com.spring_recipe.demo.service.interfaces.RecipeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;
import org.springframework.web.bind.annotation.*;



import java.util.List;

import static java.lang.String.format;

@RestController
@RequiredArgsConstructor
@Slf4j
public class RecipeController {

    private final RecipeService RecipeService;
    private final UserDetailsServiceImpl UserDetailsService;

    @GetMapping("/Recipes/all")
    @PreAuthorize("hasAuthority('read')")
    public ResponseEntity<List<Recipe>> getAllRecipes() {
        return ResponseEntity.ok(RecipeService.getAllRecipes());
    }

    @GetMapping("/Recipes/find/{name}")
    @PreAuthorize("hasAuthority('read')")
    public ResponseEntity<RecipeDto> getRecipeByName(@PathVariable String name) throws RecipeNotFoundException {
        return ResponseEntity.ok(RecipeService.getRecipeByName(name));
    }

    @PostMapping("/Recipes")
    @PreAuthorize("hasAuthority('modification')")
    public ResponseEntity<RecipeDto> createRecipe(@RequestBody CreateRecipeRequest Recipe) throws RecipeAlreadyExistException {
        return ResponseEntity.ok(RecipeService.createRecipe(Recipe));
        
    }

    @PatchMapping("/Recipes")
    @PreAuthorize("hasAuthority('modification')")
    public ResponseEntity<RecipeDto> updateRecipe(Authentication auth, @RequestBody Recipe Recipe) throws RecipeNotFoundException{
        UserDetails udt1 = (UserDetails) auth.getPrincipal();
        int id = (RecipeService.getRecipeByName(Recipe.getName())).getUserId();
        UserDetails udt2 = UserDetailsService.findById(id);
        if (udt1.equals(udt2)){
            return ResponseEntity.ok(RecipeService.updateRecipe(Recipe));
        }
        throw new AccessDeniedException("403 returned");

    }

    @DeleteMapping("/Recipes/{id}")
    @PreAuthorize("hasAuthority('modification')")
    public ResponseEntity deleteRecipe(Authentication auth, @PathVariable String id) throws RecipeNotFoundException{
        UserDetails udt1 = (UserDetails) auth.getPrincipal();
        int user_id = RecipeService.getRecipeById(id).getUserId();
        UserDetails udt2 = UserDetailsService.findById(user_id);
        if (udt1.equals(udt2)){
            RecipeService.deleteRecipe(id);
        return ResponseEntity.ok()
                .body(format("Recipe with id= %s deleted", id));
        }
        throw new AccessDeniedException("403 returned");
    }
}