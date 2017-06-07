<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
	xmlns:xs="http://www.w3.org/2001/XMLSchema" 
	xmlns:fn="http://www.w3.org/2005/xpath-functions" 
	xmlns:fixr="http://fixprotocol.io/2016/fixrepository"
	xmlns:dcterms="http://purl.org/dc/terms/" exclude-result-prefixes="fn">
	<xsl:output method="text" encoding="UTF-8"/>
	<xsl:template match="/">
{
	"swagger": "2.0",
        <xsl:apply-templates/>
}
	</xsl:template>
	<xsl:template match="fixr:repository">
		<xsl:apply-templates/>
	</xsl:template>
	<xsl:template match="fixr:metadata">
    "info": {
		"title": "<xsl:value-of select="dcterms:title"/>",
		"version": "<xsl:value-of select="dcterms:hasVersion"/>",
		"description": "<xsl:value-of select="dcterms:description"/>"
	},
    </xsl:template>
	<xsl:template match="fixr:protocol">
		<xsl:apply-templates/>
	</xsl:template>
	<xsl:template match="fixr:messages">
		<xsl:if test="fixr:message/fixr:structure/fixr:component/fixr:fieldRef[@rendering='HTTP.Method']">
	"paths": {
		<xsl:apply-templates/>
	}
	</xsl:if>
	</xsl:template>
	<xsl:template match="fixr:message">
		<xsl:if test="fixr:structure/fixr:component/fixr:fieldRef[@rendering='HTTP.Method']">
		"<xsl:apply-templates select="fixr:structure/fixr:component/fixr:fieldRef[@rendering='URIPath.Relative']" mode="path"/>": {	
			"<xsl:value-of select="fixr:structure/fixr:component/fixr:fieldRef[@rendering='HTTP.Method']/fn:lower-case(@value)"/>": {
			"description": "<xsl:value-of select="fn:normalize-space(fixr:annotation/fixr:documentation)"/>",
			<xsl:if test="fixr:structure/fixr:component[2]">			
				"consumes": [
					"application/json",
				],
			</xsl:if>
			<xsl:if test="fixr:responses/fixr:response/fixr:messageRef">
				"produces": [
					"application/json",
				],
			}
			</xsl:if>
			<xsl:if test="fixr:structure/fixr:component/fixr:fieldRef[fn:contains(@rendering, 'Parameter')]">
				"parameters": [
				<xsl:apply-templates select="fixr:structure/fixr:component/fixr:fieldRef[fn:contains(@rendering, 'Parameter')]" mode="parameter"/>
				],
			</xsl:if>
			<xsl:if test="fixr:structure/fixr:component[2]">,
				"in": "body",
				"name": "body",
				"schema": {
					"type": "object",
					"items": {
						"$ref": "#/definitions/<xsl:value-of select="@name"/>"
					}
				}
			</xsl:if>
			<xsl:if test="fixr:responses/fixr:response/fixr:messageRef">
				"responses": [
				<xsl:apply-templates select="fixr:responses/fixr:response/fixr:messageRef"/>
				]
			</xsl:if>
		}
	</xsl:if>
	</xsl:template>
	<xsl:template match="fixr:messageRef">
		<xsl:variable name="responseMessage" select="/fixr:repository/fixr:protocol/fixr:messages/fixr:message[@name=fn:current()/@name and @scenario=fn:current()/@scenario]"/>
				<xsl:value-of select="$responseMessage/fixr:structure/fixr:component/fixr:fieldRef[@rendering='HTTP.StatusCode']/@value"/>: {
					"description": "<xsl:value-of select="fn:normalize-space($responseMessage/fixr:annotation/fixr:documentation)"/>"<xsl:if test="$responseMessage/fixr:structure/fixr:component[2]">,
					"schema": {
						"type": "object",
						"items": {
							"$ref": "#/definitions/<xsl:value-of select="@name"/><xsl:value-of select="@scenario"/>"
						}
					}
					</xsl:if>
				},
	</xsl:template>
	<xsl:template match="fixr:fieldRef" mode="path">/<xsl:value-of select="@value"/></xsl:template>
	<xsl:template match="fixr:fieldRef" mode="parameter">
				{
					"name": "<xsl:value-of select="@name"/>",
		<xsl:choose>
			<xsl:when test="@rendering = 'URIPath.Parameter'">"in": "path",</xsl:when>
			<xsl:when test="@rendering = 'HTTPHeader.Parameter'">"in": "header",</xsl:when>
			<xsl:when test="@rendering = 'URIQuery.Parameter'">"in": "query",</xsl:when>
			<xsl:when test="@rendering = 'Body.Parameter'">"in": "body",</xsl:when>
			<xsl:when test="@rendering = 'Form.Parameter'">"in": "formData",</xsl:when>
		</xsl:choose>
					"required": "<xsl:value-of select="@presence = 'required'"/>",
					"type": "<xsl:call-template name="datatype"><xsl:with-param name="id" select="@id"/></xsl:call-template>"
				},
	</xsl:template>
	<xsl:template name="datatype">
		<xsl:param name="id"/>
		<xsl:variable name="type" select="/fixr:repository/fixr:fields/fixr:field[@id=$id]/@type"/>
		<xsl:choose>
			<xsl:when test="$type='int' or $type='float'">number</xsl:when>
			<xsl:otherwise>string</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<xsl:template match="fixr:codeSets"/>
	<xsl:template match="fixr:abbreviations"/>
	<xsl:template match="fixr:datatypes"/>
	<xsl:template match="fixr:categories"/>
	<xsl:template match="fixr:sections"/>
	<xsl:template match="fixr:fields"/>
	<xsl:template match="fixr:components"/>
	<xsl:template match="fixr:actors"/>
	<xsl:template match="fixr:annotation"/>
</xsl:stylesheet>
