package com.ecom.trial.Repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecom.trial.Models.Tags;


public interface TagRepo extends JpaRepository<Tags, Integer> {
    Optional<Tags> findBySlug(String slug);

    List<Tags> findBySlugIn(List<String> slugs);
}