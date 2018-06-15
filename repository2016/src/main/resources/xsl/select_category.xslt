<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:fn="http://www.w3.org/2005/xpath-functions" 
xmlns:fixr="http://fixprotocol.io/2016/fixrepository" xmlns:dc="http://purl.org/dc/elements/1.1" 
exclude-result-prefixes="fn">
	<xsl:param name="selection" required="yes"/>
	<xsl:variable name="operation" select="fn:substring($selection,1,1)"/>
	<xsl:variable name="category" select="fn:substring($selection,2)"/>
	<xsl:namespace-alias stylesheet-prefix="#default" result-prefix="fixr"/>
	<xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>
	<xsl:template match="/">
		<xsl:apply-templates/>
	</xsl:template>
	<xsl:template match="fixr:repository">
		<xsl:copy>
			<xsl:copy-of select="@*"/>
			<xsl:apply-templates select="fixr:metadata"/>
			<xsl:apply-templates select="fixr:codeSets"/>
			<xsl:copy-of select="fixr:abbreviations"/>
			<xsl:copy-of select="fixr:datatypes"/>
			<xsl:copy-of select="fixr:categories"/>
			<xsl:copy-of select="fixr:sections"/>
			<xsl:apply-templates select="fixr:fields"/>
			<xsl:copy-of select="fixr:actors"/>
			<xsl:apply-templates select="fixr:components"/>
			<xsl:apply-templates select="fixr:messages"/>
		</xsl:copy>
	</xsl:template>
	<xsl:template match="fixr:metadata">
		<xsl:copy>
			<xsl:copy-of select="child::*"/>
			<dc:contributor>select_category.xslt</dc:contributor>
		</xsl:copy>
	</xsl:template>
		<xsl:template match="fixr:codeSets">
		<xsl:copy>
			<xsl:copy-of select="@*"/>
			<xsl:variable name="ids" select="distinct-values(//*[@category='Session']//fixr:fieldRef/@id)"/>
			<xsl:variable name="types" select="distinct-values(//fixr:field[@id=$ids]/@type)"/>
			<xsl:choose>
				<xsl:when test="$operation='+'">
					<xsl:apply-templates select="fixr:codeSet[@name=$types]"/>
				</xsl:when>
				<xsl:when test="$operation='-'">
					<xsl:apply-templates select="fixr:codeSet[not(@name=$types)]"/>
				</xsl:when>
			</xsl:choose>
		</xsl:copy>
	</xsl:template>
	<xsl:template match="fixr:codeSet">
		<xsl:copy-of select="."/>
	</xsl:template>
	<xsl:template match="fixr:fields">
		<xsl:copy>
			<xsl:copy-of select="@*"/>
			<xsl:variable name="references" select="distinct-values(//*[@category='Session']//fixr:fieldRef/@id)"/>
			<xsl:choose>
				<xsl:when test="$operation='+'">
					<xsl:apply-templates select="fixr:field[@id=$references]"/>
				</xsl:when>
				<xsl:when test="$operation='-'">
					<xsl:apply-templates select="fixr:field[not(@id=$references)]"/>
				</xsl:when>
			</xsl:choose>
		</xsl:copy>
	</xsl:template>
	<xsl:template match="fixr:field">
		<xsl:copy-of select="."/>
	</xsl:template>
	<xsl:template match="fixr:components">
		<xsl:copy>
			<xsl:copy-of select="@*"/>
			<xsl:choose>
				<xsl:when test="$operation='+'">
					<xsl:apply-templates select="fixr:component[@category=$category]|fixr:group[@category=$category]"/>
				</xsl:when>
				<xsl:when test="$operation='-'">
					<xsl:apply-templates select="fixr:component[not(@category=$category)]|fixr:group[not(@category=$category)]"/>
				</xsl:when>
			</xsl:choose>
		</xsl:copy>
	</xsl:template>
	<xsl:template match="fixr:component">
		<xsl:copy-of select="."/>
	</xsl:template>
	<xsl:template match="fixr:group">
		<xsl:copy-of select="."/>
	</xsl:template>
	<xsl:template match="fixr:messages">
		<xsl:copy>
			<xsl:copy-of select="@*"/>
			<xsl:choose>
				<xsl:when test="$operation='+'">
					<xsl:apply-templates select="fixr:message[@category=$category]"/>
				</xsl:when>
				<xsl:when test="$operation='-'">
					<xsl:apply-templates select="fixr:message[not(@category=$category)]"/>
				</xsl:when>
			</xsl:choose>
		</xsl:copy>
	</xsl:template>
	<xsl:template match="fixr:message">
		<xsl:copy-of select="."/>
	</xsl:template>
</xsl:stylesheet>
