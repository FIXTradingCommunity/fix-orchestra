<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:fn="http://www.w3.org/2005/xpath-functions" xmlns:fixr="http://fixprotocol.io/2016/fixrepository" xmlns:dc="http://purl.org/dc/elements/1.1/" exclude-result-prefixes="fn fixr dc">
	<xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>
	<xsl:template match="/">
		<xsl:apply-templates/>
		<xsl:variable name="version" select="/fixr:repository/@version"/>
		<xsl:result-document href="{$version}_en_phrases.xml" method="xml" exclude-result-prefixes="fn fixr dc">
			<phrases langId="en">
				<xsl:attribute name="version"><xsl:value-of select="$version"/></xsl:attribute>
				<xsl:attribute name="generated"><xsl:value-of select="fn:current-dateTime()"/></xsl:attribute>
				<xsl:apply-templates select="//fixr:annotation" mode="phrases"/>
			</phrases>
		</xsl:result-document>
	</xsl:template>
	<xsl:template match="fixr:repository">
		<fixRepository edition="2010">
			<xsl:attribute name="generated"><xsl:value-of select="fn:current-dateTime()"/></xsl:attribute>
			<xsl:attribute name="copyright"><xsl:value-of select="/fixr:repository/fixr:metadata/dc:rights"/></xsl:attribute>
			<fix fixml="1">
				<xsl:attribute name="version"><xsl:value-of select="./@version"/></xsl:attribute>
				<xsl:attribute name="hasComponents"><xsl:choose><xsl:when test="./fixr:components">1</xsl:when><xsl:otherwise>0</xsl:otherwise></xsl:choose></xsl:attribute>
				<xsl:apply-templates select="fixr:datatypes"/>
				<xsl:apply-templates select="fixr:categories"/>
				<xsl:apply-templates select="fixr:sections"/>
				<xsl:apply-templates select="fixr:fields"/>
				<components>
					<xsl:apply-templates select="fixr:components/@*"/>
					<xsl:apply-templates select="fixr:components/*"/>
					<xsl:apply-templates select="fixr:groups/*"/>
				</components>
				<xsl:apply-templates select="fixr:messages"/>
			</fix>
		</fixRepository>
	</xsl:template>
	<xsl:template match="fixr:datatypes">
		<datatypes>
			<xsl:apply-templates/>
		</datatypes>
	</xsl:template>
	<xsl:template match="fixr:datatype">
		<datatype>
			<xsl:apply-templates select="@* except @supported"/>
			<xsl:if test="fixr:annotation/fixr:documentation">
				<xsl:attribute name="textId" select="fn:concat('DT_', @name)"/>
			</xsl:if>
			<xsl:apply-templates select="fixr:mappedDatatype[@standard='XML']"/>
		</datatype>
	</xsl:template>
	<xsl:template match="fixr:mappedDatatype">
		<XML>
			<xsl:apply-templates select="@* except @standard"/>
		</XML>
	</xsl:template>
	<xsl:template match="fixr:categories">
		<categories>
			<xsl:apply-templates/>
		</categories>
	</xsl:template>
	<xsl:template match="fixr:category">
		<category>
			<xsl:apply-templates select="@* except @name except @supported"/>
			<xsl:attribute name="id" select="@name"/>
		</category>
	</xsl:template>
	<xsl:template match="fixr:sections">
		<sections>
			<xsl:apply-templates/>
		</sections>
	</xsl:template>
	<xsl:template match="fixr:section">
		<section>
			<xsl:apply-templates select="@* except @supported"/>
			<xsl:attribute name="id" select="@name"/>
			<xsl:if test="fixr:annotation/fixr:documentation">
				<xsl:attribute name="textId" select="fn:concat('SCT_', @name)"/>
			</xsl:if>
		</section>
	</xsl:template>
	<xsl:template match="fixr:fields">
		<fields>
			<xsl:apply-templates select="@*"/>
			<xsl:apply-templates/>
		</fields>
	</xsl:template>
	<xsl:template match="fixr:field">
		<field>
			<xsl:apply-templates select="@* except (@type, @discriminatorId, @discriminatorName, @lengthId, @lengthName, @scenario, @presence, @supported)"/>
			<xsl:if test="fixr:annotation/fixr:documentation">
				<xsl:attribute name="textId" select="fn:concat('FIELD_', @id)"/>
			</xsl:if>
			<xsl:choose>
				<xsl:when test="/fixr:repository/fixr:codeSets/fixr:codeSet[@name = current()/@type]">
					<xsl:attribute name="type" select="/fixr:repository/fixr:codeSets/fixr:codeSet[@name = current()/@type][1]/@type"/>
					<xsl:call-template name="enums">
						<xsl:with-param name="codeSetName" select="/fixr:repository/fixr:codeSets/fixr:codeSet[@name = current()/@type][1]/@name"/>
					</xsl:call-template>
				</xsl:when>
				<xsl:otherwise>
					<xsl:apply-templates select="@type"/>
				</xsl:otherwise>
			</xsl:choose>
			<xsl:if test="@type='Length'">
				<xsl:attribute name="associatedDataTag" select="/fixr:repository/fixr:fields/fixr:field[@lengthId=current()/@id]/@id"/>
			</xsl:if>
		</field>
	</xsl:template>
	<xsl:template name="enums">
		<xsl:param name="codeSetName"/>
		<xsl:for-each select="/fixr:repository/fixr:codeSets/fixr:codeSet[@name = $codeSetName]">
			<xsl:apply-templates/>
		</xsl:for-each>
	</xsl:template>
	<xsl:template match="fixr:code">
		<enum>
			<xsl:apply-templates select="@* except (@name, @id, @scenario, @supported)"/>
			<xsl:attribute name="symbolicName" select="@name"/>
			<xsl:if test="fixr:annotation/fixr:documentation">
				<xsl:attribute name="textId" select="fn:concat('ENUM_', ../@id, '_', @value)"/>
			</xsl:if>
			<xsl:if test="not(@supported='supported')">
				<xsl:attribute name="supported">0</xsl:attribute>
			</xsl:if>
		</enum>
	</xsl:template>
	<xsl:template match="fixr:component">
		<component>
			<xsl:apply-templates select="@* except (@scenario, @supported)"/>
			<xsl:attribute name="repeating">0</xsl:attribute>
			<xsl:attribute name="type">Block</xsl:attribute>
			<xsl:if test="fixr:annotation/fixr:documentation">
				<xsl:attribute name="textId" select="fn:concat('COMP_', @name, '_TITLE')"/>
			</xsl:if>
			<xsl:apply-templates/>
		</component>
	</xsl:template>
	<xsl:template match="fixr:group">
		<component>
			<xsl:apply-templates select="@* except (@scenario, @supported, @implMinOccurs, @implMaxOccurs)"/>
			<xsl:attribute name="repeating">1</xsl:attribute>
			<xsl:attribute name="type">BlockRepeating</xsl:attribute>
			<xsl:if test="fixr:annotation/fixr:documentation">
				<xsl:attribute name="textId" select="fn:concat('COMP_', @name, '_TITLE')"/>
			</xsl:if>
			<repeatingGroup>
				<xsl:attribute name="id" select="current()/fixr:numInGroup/@id"/>
				<!-- required attribute but it always was empty -->
				<xsl:attribute name="name"/>
				<!-- Have no source for old but required attributes -->
				<xsl:attribute name="legacyIndent">0</xsl:attribute>
				<xsl:attribute name="legacyPosition">0</xsl:attribute>
				<xsl:apply-templates/>
			</repeatingGroup>
		</component>
	</xsl:template>
	<xsl:template match="fixr:messages">
		<messages>
			<xsl:apply-templates select="@*"/>
			<xsl:apply-templates/>
		</messages>
	</xsl:template>
	<xsl:template match="fixr:message">
		<message>
			<xsl:apply-templates select="@* except (@scenario, @supported, @flow)"/>
			<xsl:if test="fixr:annotation/fixr:documentation">
				<xsl:attribute name="textId" select="fn:concat('MSG_', @id, '_TITLE')"/>
			</xsl:if>
			<xsl:attribute name="section" select="/fixr:repository/fixr:categories/fixr:category[@name=current()/@category]/@section"/>
			<xsl:attribute name="notReqXML">0</xsl:attribute>
			<xsl:apply-templates/>
		</message>
	</xsl:template>
	<xsl:template match="fixr:responses"/>
	<xsl:template match="fixr:fieldRef">
		<fieldRef>
			<xsl:apply-templates select="@* except (@scenario, @supported, @presence)"/>
			<xsl:attribute name="name" select="/fixr:repository/fixr:fields/fixr:field[@id = current()/@id]/@name"/>
			<xsl:choose>
				<xsl:when test="@presence='required'">
					<xsl:attribute name="required">1</xsl:attribute>
				</xsl:when>
				<xsl:otherwise>
					<xsl:attribute name="required">0</xsl:attribute>
				</xsl:otherwise>
			</xsl:choose>
			<xsl:if test="not(@supported='supported') or @presence='forbidden'">
				<xsl:attribute name="supported">0</xsl:attribute>
			</xsl:if>
			<xsl:if test="fixr:annotation/fixr:documentation">
				<xsl:variable name="parent" select=".."/>
				<xsl:choose>
					<xsl:when test="fn:local-name($parent)='structure'">
						<xsl:attribute name="textId" select="fn:concat('MSG_', $parent/../@id, '_REF_', @id)"/>
					</xsl:when>
					<xsl:when test="fn:local-name($parent)='component' or fn:local-name($parent)='group'">
						<xsl:attribute name="textId" select="fn:concat('CMP_', $parent/@name, '_REF_', @id)"/>
					</xsl:when>
				</xsl:choose>
			</xsl:if>
			<!-- Have no source for old but required attributes -->
			<xsl:attribute name="legacyIndent">0</xsl:attribute>
			<xsl:attribute name="legacyPosition">0</xsl:attribute>
		</fieldRef>
	</xsl:template>
	<xsl:template match="fixr:componentRef">
		<componentRef>
			<xsl:apply-templates select="@* except (@scenario, @supported, @presence, @implMaxOccurs)"/>
			<xsl:variable name="name" select="/fixr:repository/fixr:components/fixr:component[@id = current()/@id]/@name"/>
			<xsl:attribute name="name" select="$name"/>
			<xsl:choose>
				<xsl:when test="@presence='required'">
					<xsl:attribute name="required">1</xsl:attribute>
				</xsl:when>
				<xsl:otherwise>
					<xsl:attribute name="required">0</xsl:attribute>
				</xsl:otherwise>
			</xsl:choose>
			<xsl:if test="not(@supported='supported') or @presence='forbidden'">
				<xsl:attribute name="supported">0</xsl:attribute>
			</xsl:if>
			<xsl:if test="fixr:annotation/fixr:documentation">
				<xsl:variable name="parent" select=".."/>
				<xsl:choose>
					<xsl:when test="fn:local-name($parent)='structure'">
						<xsl:attribute name="textId" select="fn:concat('MSG_', $parent/../@id, '_REF_', $name)"/>
					</xsl:when>
					<xsl:when test="fn:local-name($parent)='component' or fn:local-name($parent)='group'">
						<xsl:attribute name="textId" select="fn:concat('CMP_', $parent/@id, '_REF_', $name)"/>
					</xsl:when>
				</xsl:choose>
			</xsl:if>
			<!-- Have no source for old but required attributes -->
			<xsl:attribute name="legacyIndent">0</xsl:attribute>
			<xsl:attribute name="legacyPosition">0</xsl:attribute>
		</componentRef>
	</xsl:template>
	<xsl:template match="fixr:groupRef">
		<componentRef>
			<xsl:apply-templates select="@* except (@scenario, @supported, @presence, @implMaxOccurs)"/>
			<xsl:variable name="name" select="/fixr:repository/fixr:groups/fixr:group[@id = current()/@id]/@name"/>
			<xsl:attribute name="name" select="$name"/>
			<xsl:choose>
				<xsl:when test="@presence='required'">
					<xsl:attribute name="required">1</xsl:attribute>
				</xsl:when>
				<xsl:otherwise>
					<xsl:attribute name="required">0</xsl:attribute>
				</xsl:otherwise>
			</xsl:choose>
			<xsl:if test="not(@supported='supported') or @presence='forbidden'">
				<xsl:attribute name="supported">0</xsl:attribute>
			</xsl:if>
			<xsl:if test="fixr:annotation/fixr:documentation">
				<xsl:variable name="parent" select=".."/>
				<xsl:choose>
					<xsl:when test="fn:local-name($parent)='structure'">
						<xsl:attribute name="textId" select="fn:concat('MSG_', $parent/../@id, '_REF_', $name)"/>
					</xsl:when>
					<xsl:when test="fn:local-name($parent)='component' or fn:local-name($parent)='group'">
						<xsl:attribute name="textId" select="fn:concat('CMP_', $parent/@id, '_REF_', $name)"/>
					</xsl:when>
				</xsl:choose>
			</xsl:if>
			<xsl:if test="not(fn:string(@implMaxOccurs) = 'unbounded')">
				<xsl:attribute name="implMaxOccurs" select="@implMaxOccurs"/>
			</xsl:if>
			<!-- Have no source for old but required attributes -->
			<xsl:attribute name="legacyIndent">0</xsl:attribute>
			<xsl:attribute name="legacyPosition">0</xsl:attribute>
		</componentRef>
	</xsl:template>
	<xsl:template match="fixr:annotation" mode="#default"/>
	<xsl:template match="fixr:annotation" mode="phrases">
		<phrase>
			<xsl:variable name="parent" select=".."/>
			<xsl:variable name="grandParent" select="../.."/>
			<xsl:choose>
				<xsl:when test="fn:local-name($parent)='datatype'">
					<xsl:attribute name="textId" select="fn:concat('DT_', $parent/@name)"/>
				</xsl:when>
				<xsl:when test="fn:local-name($parent)='section'">
					<xsl:attribute name="textId" select="fn:concat('SCT_', $parent/@name)"/>
				</xsl:when>
				<xsl:when test="fn:local-name($parent)='field'">
					<xsl:attribute name="textId" select="fn:concat('FIELD_', $parent/@id)"/>
				</xsl:when>
				<xsl:when test="fn:local-name($parent)='code'">
					<xsl:attribute name="textId" select="fn:concat('ENUM_', $parent/../@id, '_', $parent/@value)"/>
				</xsl:when>
				<xsl:when test="fn:local-name($parent)='component' or fn:local-name($parent)='group'">
					<xsl:attribute name="textId" select="fn:concat('COMP_', $parent/@name, '_TITLE')"/>
				</xsl:when>
				<xsl:when test="fn:local-name($parent)='message'">
					<xsl:attribute name="textId" select="fn:concat('MSG_', $parent/@id, '_TITLE')"/>
				</xsl:when>
				<xsl:when test="fn:local-name($parent)='fieldRef' and fn:local-name($grandParent)='structure'">
					<xsl:attribute name="textId" select="fn:concat('MSG_', $grandParent/../@id, '_REF_', $parent/@id)"/>
				</xsl:when>
				<xsl:when test="fn:local-name($parent)='fieldRef' and (fn:local-name($grandParent)='component' or fn:local-name($grandParent)='group')">
					<xsl:attribute name="textId" select="fn:concat('CMP_', $grandParent/@id, '_REF_', $parent/@id)"/>
				</xsl:when>
				<xsl:when test="fn:local-name($parent)='componentRef' and fn:local-name($grandParent)='structure'">
					<xsl:attribute name="textId" select="fn:concat('MSG_', $grandParent/../@id, '_REF_', /fixr:repository/fixr:components/fixr:component[@id = $parent/@id]/@name)"/>
				</xsl:when>
				<xsl:when test="fn:local-name($parent)='componentRef' and (fn:local-name($grandParent)='component' or fn:local-name($grandParent)='group')">
					<xsl:attribute name="textId" select="fn:concat('CMP_', $parent/@id, '_REF_', /fixr:repository/fixr:components/fixr:component[@id = $parent/@id]/@name)"/>
				</xsl:when>
				<xsl:when test="fn:local-name($parent)='groupRef' and fn:local-name($grandParent)='structure'">
					<xsl:attribute name="textId" select="fn:concat('MSG_', $grandParent/../@id, '_REF_', /fixr:repository/fixr:groups/fixr:group[@id = $parent/@id]/@name)"/>
				</xsl:when>
				<xsl:when test="fn:local-name($parent)='groupRef' and (fn:local-name($grandParent)='component' or fn:local-name($grandParent)='group')">
					<xsl:attribute name="textId" select="fn:concat('CMP_', $parent/@id, '_REF_', /fixr:repository/fixr:groups/fixr:group[@id = $parent/@id]/@name)"/>
				</xsl:when>
			</xsl:choose>
			<xsl:apply-templates select="fixr:documentation" mode="phrases"/>
		</phrase>
	</xsl:template>
	<xsl:template match="fixr:documentation" mode="phrases">
		<text>
			<xsl:if test="@purpose">
				<xsl:attribute name="purpose" select="@purpose"/>
			</xsl:if>
			<para>
				<xsl:value-of select="."/>
			</para>
		</text>
	</xsl:template>
	<xsl:template match="@*">
		<xsl:copy>
			<xsl:apply-templates select="../@*"/>
		</xsl:copy>
	</xsl:template>
</xsl:stylesheet>
