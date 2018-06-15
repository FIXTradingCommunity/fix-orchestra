# XSL Transforms

## enrich_datatypes.xslt

Adds datatype mappings to ISO 11404 General-Purpose Datatypes standard to an Orchestra file. Other mappings may be added in the future.

## Repository2010to2016.xsl

Translates FIX Repository 2010 Edition to Orchestra / 2016 Edition schema.

Argument is a delimited string of phrase file URLs.

## select_category.xslt

Selects message, components, and fields by category.

Argument is a category to select or deselect. Syntax is:

+category to select only elements from that category name

-category to filter out the named category but pass through all others
