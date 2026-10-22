# Developer Guide

## Acknowledgements

{list here sources of all reused/adapted ideas, code, documentation, and third-party libraries -- include links to the original source as well}

## Design & implementation

{Describe the design and implementation of the product. Use UML diagrams and short code snippets where applicable.}

### Listing bookings: list
Displays the bookings, with optional filtering and alphabetical sorting

Format: list [r/REASON] [s/SORT_ORDER]

Square brackets indicate optional arguments, do not type them.

- REASON must be either exams or lectures
- SORT_ORDER must be either alpha for A to Z or ~alpha for Z to A sorting
- Sorting compares booking names, ignoring capitalization
- Omitting r/REASON shows all bookings
- Omitting s/SORT_ORDER shows the list with original booking orders preserved
- The arguments may be swapped to appear in any order first, and can only be specified once

Examples:

|          Command           |                Result                 |
|:--------------------------:|:-------------------------------------:|
|           `list`           |           Show all bookings           |
|       `list r/exams`       |        Show all exam bookings         |
|       `list s/alpha`       | Show all bookings sorted from A to Z  |
| `list r/lectures s/~alpha` | Show all lectures sorted from Z to A  |       

![List command sequence diagram](UMLDiagrams/ListCommandSequenceDiagram.png)

## Product scope
### Target user profile

{Describe the target user profile}

### Value proposition

{Describe the value proposition: what problem does it solve?}

## User Stories

|Version| As a ... | I want to ... | So that I can ...|
|--------|----------|---------------|------------------|
|v1.0|new user|see usage instructions|refer to them when I forget how to use the application|
|v2.0|user|find a to-do item by name|locate a to-do without having to go through the entire list|

## Non-Functional Requirements

{Give non-functional requirements}

## Glossary

* *glossary item* - Definition

## Instructions for manual testing

{Give instructions on how to do a manual product testing e.g., how to load sample data to be used for testing}
