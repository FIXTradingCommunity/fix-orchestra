<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
	xmlns:xs="http://www.w3.org/2001/XMLSchema" 
	xmlns:fn="http://www.w3.org/2005/xpath-functions" 
	xmlns:fixr="http://fixprotocol.io/2016/fixrepository"
	xmlns:dcterms="http://purl.org/dc/terms/" exclude-result-prefixes="fn">
	<xsl:output method="text" encoding="UTF-8"/>
	<xsl:template match="/">
		<xsl:apply-templates/>
	</xsl:template>
	<xsl:template match="fixr:repository">
		<xsl:apply-templates/>
	</xsl:template>
	<xsl:template match="fixr:protocol">
		<xsl:apply-templates/>
	</xsl:template>
	<xsl:template match="fixr:components">
		<!-- xsl:apply-templates -->	
	</xsl:template>
	<xsl:template match="fixr:component">
		<xsl:variable name="filename" select="fn:concat('definitions/', @name, '.json')"/>
		<xsl:result-document method="text" href="{$filename}">
{ 
	"title"                : "<xsl:value-of select="@name"/>",
	"description"          : "JSON Schema for component <xsl:value-of select="@name"/>",
	"type"                 : "object",
	"properties"           : {
		<xsl:apply-templates select="fixr:fieldRef|fixr:groupRef|fixr:componentRef" mode="properties"/>
	},
	"required"             : [ 
		<xsl:apply-templates select="fixr:fieldRef|fixr:groupRef|fixr:componentRef" mode="required"/>
	]
}
		</xsl:result-document>
	</xsl:template>
	<xsl:template match="fixr:group">		<xsl:variable name="filename" select="fn:concat('definitions/', @name, '.json')"/>
		<xsl:result-document method="text" href="{$filename}">
{ 
	"title"                : "<xsl:value-of select="@name"/>",
	"description"          : "JSON Schema for repeating group <xsl:value-of select="@name"/>",
	"type"                 : "array",
	"items"                : {
		"type": "object",
		<xsl:apply-templates select="@*"/>
		"properties": {
			<xsl:apply-templates select="fixr:fieldRef|fixr:groupRef|fixr:componentRef" mode="properties"/>
		},
		"required"             : [ 
			<xsl:apply-templates select="fixr:fieldRef|fixr:groupRef|fixr:componentRef" mode="required"/>
		]
	}
}
		</xsl:result-document>
	</xsl:template>
		<xsl:template match="fixr:messages">
		<xsl:apply-templates/>	
	</xsl:template>
	<xsl:template match="fixr:message">
		<xsl:variable name="filename" select="fn:concat('definitions/', @name, @scenario, '.json')"/>
		<xsl:result-document method="text" href="{$filename}">
{ 
	"$schema"              : "http://json-schema.org/draft-04/schema#",
	"title"                : "<xsl:value-of select="@name"/>",
	"description"          : "JSON Schema for message <xsl:value-of select="@name"/>",
	"type"                 : "object",
	"properties"           : {
		<xsl:apply-templates mode="properties"/>
	},
	"required"             : [ 
		<xsl:apply-templates mode="required"/>
	]
}
		</xsl:result-document>
	</xsl:template>
	<xsl:template match="fixr:structure" mode="#all">
		<xsl:apply-templates select="fixr:fieldRef|fixr:groupRef|fixr:componentRef" mode="#current"/>
	</xsl:template>
	<xsl:template match="fixr:fieldRef" mode="properties">
		"<xsl:value-of select="@name"/>": { 
		<xsl:call-template name="datatype"><xsl:with-param name="id" select="@id"/></xsl:call-template><xsl:apply-templates select="@*"/>
		<xsl:call-template name="enum"><xsl:with-param name="id" select="@id"/></xsl:call-template>
		}<xsl:if test="fn:position() != fn:last()">, </xsl:if>
	</xsl:template>
	<xsl:template match="fixr:fieldRef|fixr:componentRef|fixr:groupRef" mode="required">
		"<xsl:value-of select="@name"/>"<xsl:if test="fn:position() != fn:last()">, </xsl:if>
	</xsl:template>
	<xsl:template match="fixr:componentRef|fixr:groupRef" mode="properties">
		"<xsl:value-of select="@name"/>" : {"$ref": "#/definitions/<xsl:value-of select="@name"/>"}<xsl:if test="fn:position() != fn:last()">, </xsl:if>
	</xsl:template>
	<xsl:template name="datatype">
		<xsl:param name="id"/>
		<xsl:variable name="fieldType" select="/fixr:repository/fixr:fields/fixr:field[@id=$id]/@type"/>
		<xsl:variable name="codesetType" select="/fixr:repository/fixr:codeSets/fixr:codeSet[@name=$fieldType]/@type"/>
		<xsl:variable name="type" select="$codesetType|$fieldType"/>
		<xsl:choose>
			<xsl:when test="$type='int'">"type": "integer"</xsl:when>
			<xsl:when test="$type='DayOfMonth'">"type": "integer"</xsl:when>
			<xsl:when test="$type='Length'">"type": "integer"</xsl:when>
			<xsl:when test="$type='float'">"type": "number"</xsl:when>
			<xsl:when test="$type='Qty'">"type": "number"</xsl:when>
			<xsl:when test="$type='Price'">"type": "number"</xsl:when>
			<xsl:when test="$type='PriceOffset'">"type": "number"</xsl:when>
			<xsl:when test="$type='Amt'">"type": "number"</xsl:when>
			<xsl:when test="$type='Percentage'">"type": "number"</xsl:when>
			<xsl:when test="$type='Boolean'">"type": "string"</xsl:when>
			<xsl:when test="$type='UTCTimestamp'">"type": "string",
			"format" : "date-time"</xsl:when>
			<xsl:otherwise>"type" : "string"</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<xsl:template match="@minInclusive">,
			"minimum": <xsl:value-of select="fn:current()"/>
	</xsl:template>
	<xsl:template match="@maxInclusive">,
			"maximum": <xsl:value-of select="fn:current()"/>
	</xsl:template>
	<xsl:template match="@implMinLength">,
			"minLength": <xsl:value-of select="fn:current()"/>
	</xsl:template>
	<xsl:template match="@implMaxLength">,
			"maxLength": <xsl:value-of select="fn:current()"/>
	</xsl:template>
	<xsl:template match="@implMinOccurs">
			"minItems": <xsl:value-of select="fn:current()"/>,
	</xsl:template>
	<xsl:template match="@implMaxOccurs">
			"maxItems": <xsl:value-of select="fn:current()"/>,
	</xsl:template>
	<xsl:template name="enum">
		<xsl:param name="id"/>
		<xsl:variable name="fieldType" select="/fixr:repository/fixr:fields/fixr:field[@id=$id]/@type"/>
		<xsl:variable name="codesetType" select="/fixr:repository/fixr:codeSets/fixr:codeSet[@name=$fieldType]/@type"/>
		<xsl:if test="/fixr:repository/fixr:codeSets/fixr:codeSet[@name=$fieldType]">,
			"enum": [
			<xsl:for-each select="/fixr:repository/fixr:codeSets/fixr:codeSet[@name=$fieldType]/fixr:code">
				<xsl:choose>
					<xsl:when test="$codesetType='int'"><xsl:value-of select="@value"/></xsl:when>
					<xsl:otherwise>"<xsl:value-of select="@value"/>"</xsl:otherwise>
			</xsl:choose>
		<xsl:if test="fn:position() != fn:last()">, </xsl:if>
		</xsl:for-each>
			]
		</xsl:if>
	</xsl:template>
	<xsl:template match="fixr:metadata"/>
	<xsl:template match="fixr:codeSets"/>
	<xsl:template match="fixr:abbreviations"/>
	<xsl:template match="fixr:datatypes"/>
	<xsl:template match="fixr:categories"/>
	<xsl:template match="fixr:sections"/>
	<xsl:template match="fixr:fields"/>
	<xsl:template match="fixr:actors"/>
	<xsl:template match="fixr:annotation" mode="#all"/>
	<xsl:template match="fixr:responses"/>
	<xsl:template match="@*" mode="#all"/>
</xsl:stylesheet>
