<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:fn="http://www.w3.org/2005/xpath-functions" xmlns:functx="http://www.functx.com" xmlns:fixr="http://fixprotocol.io/2020/orchestra/repository" xmlns:dc="http://purl.org/dc/elements/1.1/" version="2.0" exclude-result-prefixes="fn functx">
	<!-- argument is phrase file URL, e.g. file://FIX.5.0SP2_en_phrases.xml" -->
	<xsl:param name="phrases-file"/>
	<xsl:param name="name"/>
	<xsl:param name="new-version"/>
	<xsl:output method="xml" encoding="UTF-8" indent="yes"/>
	<xsl:namespace-alias stylesheet-prefix="#default" result-prefix="fixr"/>
	<xsl:variable name="phrases-doc" select="fn:document($phrases-file)"/>
	<xsl:variable name="old-version" select="$phrases-doc/phrases/@version"/>
	<xsl:key name="phrases-key" match="phrase" use="@textId"/>
	<xsl:template match="/">
		<xsl:apply-templates/>
	</xsl:template>
	<xsl:template match="fixRepository">
		<fixr:repository>
			<xsl:apply-templates select="//fix[@version=$old-version]/@* except //fix[@version=$old-version]/@version except //fix[@version=$old-version]/@components"/>
			<xsl:attribute name="name"><xsl:value-of select="$name"/></xsl:attribute>
			<xsl:attribute name="version"><xsl:value-of select="$new-version"/></xsl:attribute>
			<metadata>
				<dc:title>Orchestra</dc:title>
				<dc:creator>unified2orchestra.xslt script</dc:creator>
				<dc:publisher>FIX Trading Community</dc:publisher>
				<dc:date>
					<xsl:value-of select="fn:current-dateTime()"/>
				</dc:date>
				<dc:format>Orchestra schema</dc:format>
				<dc:source>FIX Unified Repository</dc:source>
				<xsl:if test="./@copyright">
				<dc:rights><xsl:value-of select="./@copyright"/></dc:rights>
				</xsl:if>
			</metadata>
			<codeSets>
				<!-- Need to store context outside of for-each loop -->
				<xsl:variable name="doc" select="/"/>
				<!-- Add codesets for fields that have enums from latest fix version to contain it -->
				<xsl:for-each select="fn:distinct-values(/fixRepository/fix[@version=$old-version]/fields/field[enum]/@name)">
					<xsl:variable name="fieldName" select="."/>
					<xsl:variable name="field" select="($doc/fixRepository/fix[@version=$old-version]/fields/field[@name=$fieldName])"/>
					<xsl:variable name="fieldId" select="$field/@id"/>
					<xsl:variable name="fieldType" select="$field/@type"/>
					<xsl:element name="fixr:codeSet">
						<xsl:attribute name="name"><xsl:value-of select="concat($fieldName, 'CodeSet')"/></xsl:attribute>
						<xsl:attribute name="id"><xsl:value-of select="$fieldId"/></xsl:attribute>
						<xsl:attribute name="type"><xsl:value-of select="$fieldType"/></xsl:attribute>
						<!-- copy pedigree from field -->
						<xsl:apply-templates select="$field/@added"/> 
						<xsl:apply-templates select="$field/@addedEP"/> 
						<xsl:apply-templates select="$field/@updated"/> 
						<xsl:apply-templates select="$field/@updatedEP"/> 
						<xsl:apply-templates select="$field/@deprecated"/> 
						<xsl:apply-templates select="$field/@deprecatedEP"/> 
						<xsl:for-each select="$field/enum">
							<xsl:element name="fixr:code">
								<xsl:attribute name="name"><xsl:value-of select="current()/@symbolicName"/></xsl:attribute>
								<xsl:attribute name="id"><xsl:value-of select="concat($fieldId, 
									substring(concat('000', fn:position()), string-length(concat('000', fn:position()))-2, 3))"/></xsl:attribute>
								<xsl:apply-templates select="@* except @symbolicName"/>
							</xsl:element>
						</xsl:for-each>
						<xsl:apply-templates select="$field/@textId"/>
					</xsl:element>
				</xsl:for-each>
			</codeSets>
			<xsl:apply-templates select="//fix[@version=$old-version]/datatypes"/>
			<xsl:apply-templates select="//fix[@version=$old-version]/categories"/>
			<xsl:apply-templates select="//fix[@version=$old-version]/sections"/>
			<xsl:apply-templates select="//fix[@version=$old-version]/fields"/>
			<xsl:apply-templates select="//fix[@version=$old-version]/components" mode="component"/>
			<xsl:apply-templates select="//fix[@version=$old-version]/components" mode="group"/>
			<xsl:apply-templates select="//fix[@version=$old-version]/messages"/>
		</fixr:repository>
	</xsl:template>
	<xsl:template match="datatypes">
		<fixr:datatypes>
			<xsl:apply-templates/>
		</fixr:datatypes>
	</xsl:template>
	<xsl:template match="datatype">
		<fixr:datatype>
			<xsl:apply-templates select="@* except @textId"/>
			<xsl:apply-templates select="XML"/>
			<fixr:annotation>
				<xsl:for-each select="fn:key('phrases-key', @textId, $phrases-doc)//text">
					<xsl:element name="fixr:documentation">
						<xsl:apply-templates select="@purpose"/>
						<xsl:value-of select="."/>
					</xsl:element>
				</xsl:for-each>
				<xsl:apply-templates select="Example"/>
			</fixr:annotation>
		</fixr:datatype>
	</xsl:template>
	<xsl:template match="Example">
		<fixr:documentation purpose="EXAMPLE">
			<xsl:value-of select="current()"/>
		</fixr:documentation>
	</xsl:template>
	<xsl:template match="text">
		<fixr:documentation>
			<xsl:apply-templates select="@*"/>
			<xsl:apply-templates/>
		</fixr:documentation>
	</xsl:template>
	<xsl:template match="para">
		<xsl:value-of select="current()"/>
	</xsl:template>
	<xsl:template match="XML">
		<mappedDatatype standard="XML">
			<xsl:apply-templates select="@*"/>
		</mappedDatatype>
	</xsl:template>
	<xsl:template match="categories">
		<fixr:categories>
			<xsl:apply-templates/>
		</fixr:categories>
	</xsl:template>
	<xsl:template match="category">
		<fixr:category>
			<xsl:apply-templates select="@* except @textId except @volume except @id"/>
			<xsl:attribute name="name" select="@id"/>
			<xsl:apply-templates select="@textId"/>
		</fixr:category>
	</xsl:template>
	<xsl:template match="sections">
		<fixr:sections>
			<xsl:apply-templates/>
		</fixr:sections>
	</xsl:template>
	<xsl:template match="section">
		<fixr:section>
			<xsl:apply-templates select="@* except @textId except @volume except @id"/>
			<xsl:attribute name="name" select="@id"/>
			<xsl:apply-templates/>
			<xsl:apply-templates select="@textId"/>
		</fixr:section>
	</xsl:template>
	<xsl:template match="fields">
		<fixr:fields>
			<xsl:apply-templates select="@*"/>
			<xsl:apply-templates/>
		</fixr:fields>
	</xsl:template>
	<xsl:template match="field">
		<fixr:field>
			<xsl:apply-templates select="@* except @textId"/>
			<!-- Assumes that discriminator for field X follows naming convention XSource -->
			<xsl:variable name="discriminator" select="../field[@name = fn:concat(current()/@name, 'Source')]"/>
			<xsl:if test="$discriminator">
				<xsl:attribute name="discriminatorId" select="$discriminator/@id"/>
			</xsl:if>
			<xsl:choose>
				<xsl:when test="current()/enum">
					<xsl:attribute name="type" select="concat(@name, 'CodeSet')"/>
				</xsl:when>
				<xsl:when test="@type = 'data' or @type = 'XMLData'">
					<xsl:variable name="length" select="../field[@associatedDataTag = current()/@id]"/>
					<xsl:attribute name="lengthId" select="$length/@id"/>
				</xsl:when>
			</xsl:choose>
			<xsl:apply-templates select="@textId"/>
		</fixr:field>
	</xsl:template>
	<xsl:template match="components" mode="component">
		<fixr:components>
			<xsl:apply-templates select="@*"/>
			<!-- repeating attribute not always present; assume its a component rather than a group then -->
			<xsl:apply-templates select="component[not(@repeating=1)]"/>
		</fixr:components>
	</xsl:template>
	<xsl:template match="components" mode="group">
		<fixr:groups>
			<xsl:apply-templates select="@*"/>
			<!-- FIX 4.4 or later -->
			<xsl:apply-templates select="component[@repeating=1]" mode="group"/>
			<!-- FIX 4.2 -->
			<!-- Need to store context outside of for-each loop -->
			<xsl:variable name="fix" select="ancestor::fix"/>
			<xsl:for-each select="fn:distinct-values(ancestor::fix//repeatingGroup[not(name(parent::*)='component')]/@id)">
				<xsl:variable name="id" select="."/>
				<xsl:apply-templates select="($fix//repeatingGroup[@id=$id])[last()]" mode="group"/>
			</xsl:for-each>
		</fixr:groups>
	</xsl:template>
	<xsl:template match="component">
		<xsl:choose>
			<xsl:when test="@repeating = '1'">
				<xsl:apply-templates/>
			</xsl:when>
			<xsl:otherwise>
				<fixr:component>
					<xsl:apply-templates select="@* except @type except @textId"/>
					<xsl:apply-templates mode="member"/>
					<xsl:apply-templates select="@textId"/>
				</fixr:component>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<xsl:template match="repeatingGroup" mode="group">
		<group>
			<xsl:apply-templates select="@* except @required except @textId"/>
			<!-- Find component for this repeating group. If FIX 4.4 or later, it will be the parent element. Otherwise, search for last component with matching NumInGroup id. -->
			<xsl:choose>
				<xsl:when test="name(..)='component'">
					<xsl:variable name="parentComponent" select=".."/>
					<xsl:attribute name="id" select="$parentComponent/@id"/>
					<xsl:attribute name="name" select="$parentComponent/@name"/>
					<xsl:attribute name="category" select="$parentComponent/@category"/>
					<xsl:attribute name="abbrName" select="$parentComponent/@abbrName"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:variable name="parentComponent" select="(//component[repeatingGroup/@id=current()/@id])[last()]"/>
					<xsl:attribute name="id" select="$parentComponent/@id"/>
					<xsl:attribute name="name" select="$parentComponent/@name"/>
					<xsl:attribute name="category" select="$parentComponent/@category"/>
					<xsl:attribute name="abbrName" select="$parentComponent/@abbrName"/>
				</xsl:otherwise>
			</xsl:choose>
			<xsl:element name="fixr:numInGroup">
				<xsl:attribute name="id" select="@id"/>
				<xsl:apply-templates select="@textId"/>
			</xsl:element>
			<xsl:apply-templates mode="member"/>
			<xsl:apply-templates select="../@textId"/>
		</group>
	</xsl:template>
	<xsl:template match="repeatingGroup" mode="member">
		<fixr:groupRef>
			<xsl:apply-templates select="@* except @name except @id except @textId"/>
			<!-- id is the NumInGroup tag, not the group id -->
			<xsl:choose>
				<xsl:when test="name(..)='component'">
					<xsl:variable name="parentComponent" select=".."/>
					<xsl:attribute name="id" select="$parentComponent/@id"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:variable name="parentComponent" select="(//component[repeatingGroup/@id=current()/@id])[last()]"/>
					<xsl:attribute name="id" select="$parentComponent/@id"/>
				</xsl:otherwise>
			</xsl:choose>
			<xsl:apply-templates select="@textId"/>
		</fixr:groupRef>
	</xsl:template>
	<xsl:template match="messages">
		<fixr:messages>
			<xsl:apply-templates select="@*"/>
			<xsl:apply-templates/>
		</fixr:messages>
	</xsl:template>
	<xsl:template match="message">
		<fixr:message>
			<xsl:apply-templates select="@* except @textId except @section"/>
			<fixr:structure>
				<xsl:apply-templates mode="member"/>
			</fixr:structure>
			<xsl:apply-templates select="@textId"/>
		</fixr:message>
	</xsl:template>
	<xsl:template match="componentRef" mode="#all">
		<xsl:choose>
			<xsl:when test="//component[@id=current()/@id and @repeating=1]">
				<fixr:groupRef>
					<xsl:apply-templates select="@* except @name except @textId"/>
					<xsl:apply-templates select="@textId"/>
				</fixr:groupRef>
			</xsl:when>
			<xsl:otherwise>
				<fixr:componentRef>
					<xsl:apply-templates select="@* except @name except @textId"/>
					<xsl:apply-templates select="@textId"/>
				</fixr:componentRef>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<xsl:template match="fieldRef" mode="#all">
		<fixr:fieldRef>
			<xsl:apply-templates select="@* except @name except @textId"/>
			<xsl:apply-templates select="@textId"/>
		</fixr:fieldRef>
	</xsl:template>
	<xsl:template match="@enumDatatype">
		<xsl:variable name="fieldName" select="../../field[@id=current()]/@name"/>
		<xsl:attribute name="type" select="concat($fieldName, 'CodeSet')"/>
	</xsl:template>
	<xsl:template match="@addedEP">
		<xsl:if test="current() != -1">
			<xsl:copy/>
		</xsl:if>
	</xsl:template>
	<xsl:template match="@required">
		<xsl:if test="current() = 1">
			<xsl:attribute name="presence">required</xsl:attribute>
		</xsl:if>
	</xsl:template>
	<!-- don't copy deprecated attributes -->
	<xsl:template match="@elaborationTextId"/>
	<xsl:template match="@fixml"/>
	<xsl:template match="@notReqXML"/>
	<xsl:template match="@generateImplFile"/>
	<xsl:template match="@legacyIndent"/>
	<xsl:template match="@legacyPosition"/>
	<xsl:template match="@inlined"/>
	<xsl:template match="@repeating"/>
	<xsl:template match="@associatedDataTag"/>
	<!-- copy attributes by default -->
	<xsl:template match="@*">
		<xsl:if test="not(string(.) = '')">
			<xsl:copy>
				<xsl:apply-templates select="../@*"/>
			</xsl:copy>
		</xsl:if>
	</xsl:template>
	<xsl:template match="@textId">
		<xsl:element name="fixr:annotation">
			<xsl:for-each select="fn:key('phrases-key', ../@textId, $phrases-doc)/text">
				<xsl:element name="fixr:documentation">
					<xsl:apply-templates select="@purpose"/>
					<xsl:value-of select="."/>
				</xsl:element>
			</xsl:for-each>
		</xsl:element>
	</xsl:template>
	<xsl:template match="@purpose">
		<xsl:attribute name="purpose"><xsl:value-of select="."/></xsl:attribute>
	</xsl:template>
	<xsl:function name="functx:substring-before-if-contains" as="xs:string?">
		<xsl:param name="arg" as="xs:string?"/>
		<xsl:param name="delim" as="xs:string"/>
		<xsl:sequence select="
	   if (contains($arg,$delim))
	   then substring-before($arg,$delim)
	   else $arg
	 "/>
	</xsl:function>
</xsl:stylesheet>
