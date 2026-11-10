package dto;

import model.Category;

import java.util.Map;

public record LibraryStats(int authors, int books, int rented, Map<Category, Integer> byCategory) {}