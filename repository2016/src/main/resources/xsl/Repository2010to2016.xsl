<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
				xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:fn="http://www.w3.org/2005/xpath-functions" 
                xmlns:functx="http://www.functx.com"
                xmlns:fixr="http://fixprotocol.io/2016/fixrepository"
                xmlns:dc="http://purl.org/dc/elements/1.1/" version="2.0"
                exclude-result-prefixes="fn">
    <!-- argument is a delimited string of phrase file URLs -->
    <xsl:param name="phrases-files"/>
    <xsl:variable name="phrases-files-seq" select="fn:tokenize($phrases-files, '[,\s]')"/>
    <xsl:key name="phrases-key" match="phrase" use="@textId"/>
    <xsl:output method="xml" encoding="UTF-8" indent="yes"/>
    <xsl:namespace-alias stylesheet-prefix="#default" result-prefix="fixr"/>
    <xsl:template match="/">
        <xsl:apply-templates/>
    </xsl:template>
    <xsl:template match="fixRepository">
    <fixr:repository>
			<xsl:attribute name="name"><xsl:value-of select="functx:substring-before-if-contains(//fix[fn:last()]/@version, '_')"/></xsl:attribute>
            <xsl:apply-templates select="//fix[fn:last()]/@* except hasComponents"/>
            <metadata>
				<dc:title>Orchestra</dc:title>
                <dc:creator>Repository2010to2016.xsl script</dc:creator>
                <dc:publisher>FIX Trading Community</dc:publisher>
                <dc:date>
                    <xsl:value-of select="fn:current-dateTime()"/>
                </dc:date>
                <dc:format>Repository 2016 Edition</dc:format>
            </metadata>
            <codeSets>
				<!-- Need to store context outside of for-each loop -->
				<xsl:variable name="doc" select="/"/>
				<!-- Add codesets for fields that have enums from latest fix version to contain it -->
                <xsl:for-each select="fn:distinct-values(/fixRepository/fix/fields/field[enum]/@name)">
					<xsl:variable name="fieldName" select="."/>
					<xsl:variable name="field" select="($doc/fixRepository/fix/fields/field[@name=$fieldName])[last()]"/>
                    <xsl:variable name="fieldId" select="$field/@id"></xsl:variable>
                    <xsl:variable name="fieldType" select="$field/@type"></xsl:variable>
                    <xsl:element name="fixr:codeSet">
						<xsl:attribute name="name"><xsl:value-of select="concat($fieldName, 'CodeSet')"/></xsl:attribute>
						<xsl:attribute name="id"><xsl:value-of select="$fieldId"/></xsl:attribute>
						<xsl:attribute name="type"><xsl:value-of select="$fieldType"/></xsl:attribute>
                        <xsl:for-each select="($doc//field[@name = $fieldName])[fn:last()]/enum">
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
            <!-- Assumption: protocols are listed in the original file in version order, so last() picks latest. -->
            <xsl:apply-templates select="//fix[last()]/abbreviations"/>
            <xsl:apply-templates select="//fix[last()]/datatypes"/>
            <xsl:apply-templates select="//fix[last()]/categories"/>
            <xsl:apply-templates select="//fix[last()]/sections"/>
            <xsl:apply-templates select="//fix[last()]/fields"/>
            <xsl:apply-templates select="//fix[last()]/components"/>
            <xsl:apply-templates select="//fix[last()]/messages"/>
    </fixr:repository>
    </xsl:template>
    <xsl:template match="abbreviations">
    <fixr:abbreviations>
            <xsl:apply-templates/>
    </fixr:abbreviations>
    </xsl:template>
    <xsl:template match="abbreviation">
    <fixr:abbreviation>
			<xsl:attribute name="name"><xsl:value-of select="current()/@abbrTerm"/></xsl:attribute>
            <xsl:apply-templates select="@* except @textId except @abbrTerm"/>
            <xsl:apply-templates select="@textId"/>
    </fixr:abbreviation>
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
            <xsl:variable name="phrases-file" select="$phrases-files-seq[fn:last()]"/>
			<xsl:variable name="phrases-doc" select="fn:document($phrases-file)"/>
            <xsl:for-each select="fn:key('phrases-key', @textId, $phrases-doc)//text">
                <xsl:element name="fixr:documentation"><xsl:apply-templates select="@purpose"/><xsl:value-of select="."/></xsl:element>
            </xsl:for-each>
            <xsl:apply-templates select="Example"/>
            </fixr:annotation>
    </fixr:datatype>
    </xsl:template>
    <xsl:template match="Example">
		<fixr:documentation purpose="EXAMPLE"><xsl:value-of select="current()"/></fixr:documentation>
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
            <xsl:apply-templates select="@* except @textId"/>
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
            <xsl:apply-templates select="@* except @textId"/>
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
            <xsl:choose>
                <xsl:when test="current()/enum">
                    <xsl:attribute name="type" select="concat(@name, 'CodeSet')"/>
                </xsl:when>
                <xsl:when test="@type = 'data'">
                    <xsl:attribute name="lengthId"
                                   select="(//field[@associatedDataTag = current()/@id]/@id)[fn:last()]"/>
                    <xsl:attribute name="lengthName"
                                   select="(//field[@associatedDataTag = current()/@id]/@name)[fn:last()]"/>
                </xsl:when>
            </xsl:choose>
            <xsl:apply-templates select="@textId"/>
    </fixr:field>
    </xsl:template>
     <xsl:template match="components">
    <fixr:components>
            <xsl:apply-templates select="@*"/>
            <xsl:apply-templates/>
    </fixr:components>
    </xsl:template>
    <xsl:template match="component">
        <xsl:choose>
            <xsl:when test="@repeating = '1'">
                <xsl:apply-templates/>
            </xsl:when>
            <xsl:otherwise>
                <fixr:component>
                    <xsl:apply-templates select="@* except @type except @textId"/>
                    <xsl:apply-templates/>
                    <xsl:apply-templates select="@textId"/>
                </fixr:component>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <xsl:template match="repeatingGroup">
        <group>
            <xsl:apply-templates select="@* except @required except @textId"/>
            <xsl:attribute name="id" select="../@id"/>
            <xsl:attribute name="name" select="../@name"/>
            <xsl:attribute name="numInGroupId" select="@id"/>
            <xsl:attribute name="numInGroupName"
                           select="(//field[@id = current()/@id])[fn:last()]/@name"/>
            <xsl:attribute name="category" select="../@category"/>
            <xsl:attribute name="abbrName" select="../@abbrName"/>
            <xsl:apply-templates/>
            <xsl:apply-templates select="@textId"/>
        </group>
    </xsl:template>
    <xsl:template match="messages">
    <fixr:messages>
            <xsl:apply-templates select="@*"/>
            <xsl:apply-templates/>
    </fixr:messages>
    </xsl:template>
    <xsl:template match="message">
    <fixr:message>
            <xsl:apply-templates select="@* except @textId"/>
            <fixr:structure>
				<xsl:apply-templates/>
            </fixr:structure>
            <xsl:apply-templates select="@textId"/>
    </fixr:message>
    </xsl:template>
    <xsl:template match="componentRef">
		<xsl:choose>
			<xsl:when test="//component[@id=current()/@id]/repeatingGroup">
			    <fixr:groupRef>
				<xsl:apply-templates select="@*"/>
				</fixr:groupRef>
			</xsl:when>
			<xsl:otherwise>
				<fixr:componentRef>
				<xsl:apply-templates select="@*"/>
				</fixr:componentRef>
			</xsl:otherwise>
		</xsl:choose>
    </xsl:template>
    <xsl:template match="fieldRef">
    <fixr:fieldRef>
            <xsl:apply-templates select="@*"/>
    </fixr:fieldRef>
    </xsl:template>
    <xsl:template match="@enumDatatype">
        <xsl:variable name="fieldName" select="(//field[@id=fn:current()]/@name)[fn:last()]"/>
        <xsl:attribute name="type" select="concat($fieldName, 'CodeSet')"/>
    </xsl:template>
    <!-- name changes -->
    <xsl:template match="@components">
        <xsl:attribute name="hasComponents">
            <xsl:value-of select="current()"/>
        </xsl:attribute>
    </xsl:template>
    <xsl:template match="@addedEP">
        <xsl:if test="current() != '-1'">
            <xsl:copy/>
        </xsl:if>
    </xsl:template>
    <xsl:template match="@required">
        <xsl:if test="current() = '1'">
            <xsl:attribute name="presence">required</xsl:attribute>
        </xsl:if>
    </xsl:template>

    <!-- don't copy deprecated attributes -->
    <xsl:template match="@elaborationTextId"/>
    <xsl:template match="@fixml"/>
    <xsl:template match="@hasComponents">
		<xsl:copy/>
    </xsl:template>
    <xsl:template match="@notReqXML"/>
    <xsl:template match="@generateImplFile"/>
    <xsl:template match="@legacyIndent"/>
    <xsl:template match="@legacyPosition"/>
    <xsl:template match="@inlined"/>
    <xsl:template match="@repeating"/>
    <xsl:template match="@associatedDataTag"/>

    <!-- copy attributes by default -->
    <xsl:template match="@*">
        <xsl:if test="not(. = '')">
            <xsl:copy>
                <xsl:apply-templates select="../@*"/>
            </xsl:copy>
        </xsl:if>
    </xsl:template>

    <xsl:template match="@textId">
		<xsl:variable name="index" select="index-of(/fixRepository/fix, current()/ancestor::fix)"/>
		<xsl:variable name="phrases-file" select="fn:subsequence($phrases-files-seq, $index, 1)"/>
		<xsl:variable name="phrases-doc" select="fn:document($phrases-file)"/>
        <xsl:element name="fixr:annotation">
            <xsl:for-each select="fn:key('phrases-key', ../@textId, $phrases-doc)/text">
                <xsl:element name="fixr:documentation"><xsl:apply-templates select="@purpose"/><xsl:value-of select="."/></xsl:element>
            </xsl:for-each>
        </xsl:element>
    </xsl:template>

    <xsl:template match="@purpose">
        <xsl:attribute name="purpose">
            <xsl:value-of select="."/>
        </xsl:attribute>
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