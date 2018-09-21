<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:fn="http://www.w3.org/2005/xpath-functions" xmlns:fixr="http://fixprotocol.io/2016/fixrepository" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:sbe="http://fixprotocol.io/2017/sbe" exclude-result-prefixes="fn dc fixr" version="2.0">
	<xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>
	<xsl:namespace-alias stylesheet-prefix="#default" result-prefix="sbe"/>
	<xsl:template match="/">
		<xsl:apply-templates/>
	</xsl:template>
	<xsl:template match="fixr:repository">
		<sbe:messageSchema>
			<xsl:attribute name="package"><xsl:value-of select="fixr:metadata/dc:title"/></xsl:attribute>
			<xsl:attribute name="id">1</xsl:attribute>
			<xsl:attribute name="version">0</xsl:attribute>
			<xsl:attribute name="byteOrder">littleEndian</xsl:attribute>
			<types>
				<xsl:apply-templates select="fixr:datatypes/fixr:datatype/fixr:mappedDatatype[@standard='SBE']"/>
				<xsl:apply-templates select="fixr:codeSets/fixr:codeSet"/>
				<composite name="groupSizeEncoding">
					<type name="blockLength" primitiveType="uint16"/>
					<type name="numInGroup" primitiveType="uint16"/>
					<type name="numGroups" primitiveType="uint16"/>
					<type name="numVarDataFields" primitiveType="uint16"/>
				</composite>
				<composite name="messageHeader">
					<type name="blockLength" primitiveType="uint16"/>
					<type name="templateId" primitiveType="uint16"/>
					<type name="schemaId" primitiveType="uint16"/>
					<type name="version" primitiveType="uint16"/>
					<type name="numGroups" primitiveType="uint16"/>
					<type name="numVarDataFields" primitiveType="uint16"/>
				</composite>
			</types>
			<xsl:apply-templates select="fixr:messages/fixr:message"/>
		</sbe:messageSchema>
	</xsl:template>
	<xsl:template match="fixr:mappedDatatype">
		<xsl:copy-of select="child::*"/>
	</xsl:template>
	<xsl:template match="fixr:codeSet">
		<enum>
			<xsl:attribute name="name"><xsl:value-of select="@name"/></xsl:attribute>
			<xsl:attribute name="encodingType"><xsl:value-of select="@type"/></xsl:attribute>
			<xsl:apply-templates select="fixr:code"/>
		</enum>
	</xsl:template>
	<xsl:template match="fixr:code">
		<validValue>
			<xsl:attribute name="name"><xsl:value-of select="@name"/></xsl:attribute>
			<xsl:value-of select="@value"/>
		</validValue>
	</xsl:template>
	<xsl:template match="fixr:message">
		<message>
			<xsl:attribute name="name"><xsl:value-of select="@name"/></xsl:attribute>
			<xsl:attribute name="id"><xsl:value-of select="@id"/></xsl:attribute>
			<xsl:attribute name="semanticType"><xsl:value-of select="@msgType"/></xsl:attribute>
			<xsl:apply-templates select="fixr:structure"/>
		</message>
	</xsl:template>
	<xsl:template match="fixr:structure">
		<xsl:apply-templates/>
	</xsl:template>
	<xsl:template match="fixr:fieldRef">
		<xsl:variable name="field" select="//fixr:field[@id=fn:current()/@id]"/>
		<xsl:if test="not($field/@type='Length')">
			<field>
				<xsl:attribute name="id"><xsl:value-of select="@id"/></xsl:attribute>
				<xsl:attribute name="name"><xsl:value-of select="$field/@name"/></xsl:attribute>
				<xsl:attribute name="type"><xsl:value-of select="$field/@type"/></xsl:attribute>
			</field>
		</xsl:if>
	</xsl:template>
	<xsl:template match="fixr:componentRef">
		<xsl:variable name="component" select="//fixr:component[@id=fn:current()/@id]"/>
		<xsl:if test="not($component/@name='StandardHeader' or $component/@name='StandardTrailer')">
			<xsl:apply-templates select="$component"/>
		</xsl:if>
	</xsl:template>
	<xsl:template match="fixr:groupRef">
		<xsl:variable name="group" select="//fixr:group[@id=fn:current()/@id]"/>
		<group>
			<xsl:attribute name="id"><xsl:value-of select="@id"/></xsl:attribute>
			<xsl:attribute name="name"><xsl:value-of select="$group/@name"/></xsl:attribute>
			<xsl:attribute name="dimensionType">groupSizeEncoding</xsl:attribute>
			<xsl:apply-templates select="$group"/>
		</group>
	</xsl:template>
	<xsl:template match="fixr:annotation"/>
</xsl:stylesheet>
