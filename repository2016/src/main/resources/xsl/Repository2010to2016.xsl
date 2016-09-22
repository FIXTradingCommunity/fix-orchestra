<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:fn="http://www.w3.org/2005/xpath-functions" xmlns:fixr="http://fixprotocol.io/2016/fixrepository"
                xmlns:dc="http://purl.org/dc/elements/1.1/" version="2.0"
                exclude-result-prefixes="fn">
    <xsl:variable name="phrases-doc" select="fn:document('FIX.5.0SP2_EP208_en_phrases.xml')"/>
    <xsl:key name="phrases-key" match="phrase" use="@textId"/>
    <xsl:output method="xml" indent="yes"/>
    <xsl:namespace-alias stylesheet-prefix="#default" result-prefix="fixr"/>
    <xsl:template match="/">
        <xsl:apply-templates/>
    </xsl:template>
    <xsl:template match="fixRepository">
        <xsl:copy>
            <metadata>
                <dc:creator>Repository2010to2016</dc:creator>
                <dc:date>
                    <xsl:value-of select="fn:current-dateTime()"/>
                </dc:date>
            </metadata>
            <codeSets>
                <xsl:for-each select="/fixRepository/fix/fields/field[enum]">
                    <xsl:variable name="fieldName" select="@name"></xsl:variable>
                    <xsl:element name="{concat($fieldName, 'CodeSet')}">
                        <xsl:for-each select="//field[@name = $fieldName]/enum">
                            <xsl:element name="code">
                                <xsl:apply-templates select="@*"/>
                            </xsl:element>
                        </xsl:for-each>
                    </xsl:element>
                </xsl:for-each>
            </codeSets>
            <xsl:apply-templates select="//abbreviations[last()]"/>
            <xsl:apply-templates select="//categories[last()]"/>
            <xsl:apply-templates select="//sections[last()]"/>
            <xsl:apply-templates select="//datatypes[last()]"/>
            <xsl:apply-templates select="//fields[last()]"/>
            <xsl:apply-templates select="//fix"/>
        </xsl:copy>
    </xsl:template>
    <xsl:template match="abbreviations">
        <xsl:copy>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>
    <xsl:template match="abbreviation">
        <xsl:copy>
            <xsl:apply-templates select="@* except @textId"/>
            <xsl:apply-templates select="@textId"/>
        </xsl:copy>
    </xsl:template>
    <xsl:template match="datatypes">
        <xsl:copy>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>
    <xsl:template match="datatype">
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>
    <xsl:template match="Example">
        <phrase>
            <text purpose="EXAMPLE">
                <para>
                    <xsl:value-of select="current()"/>
                </para>
            </text>
        </phrase>
    </xsl:template>
    <xsl:template match="XML">
        <mappedDatatype standard="XML">
            <xsl:apply-templates select="@*"/>
        </mappedDatatype>
    </xsl:template>
    <xsl:template match="categories">
        <xsl:copy>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>
    <xsl:template match="category">
        <xsl:copy>
            <xsl:apply-templates select="@* except @textId"/>
            <xsl:apply-templates/>
            <xsl:apply-templates select="@textId"/>
        </xsl:copy>
    </xsl:template>
    <xsl:template match="sections">
        <xsl:copy>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>
    <xsl:template match="section">
        <xsl:copy>
            <xsl:apply-templates select="@* except @textId"/>
            <xsl:apply-templates/>
            <xsl:apply-templates select="@textId"/>
        </xsl:copy>
    </xsl:template>
    <xsl:template match="fields">
        <xsl:copy>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>
    <xsl:template match="field">
        <xsl:copy>
            <xsl:apply-templates select="@* except @textId"/>
            <xsl:choose>
                <xsl:when test="current()/enum">
                    <xsl:attribute name="codeSet" select="concat(@name, 'CodeSet')"/>
                </xsl:when>
                <xsl:when test="@type = 'data'">
                    <xsl:attribute name="lengthId"
                                   select="//field[@associatedDataTag = current()/@id]/@id"/>
                    <xsl:attribute name="lengthName"
                                   select="//field[@associatedDataTag = current()/@id]/@name"/>
                </xsl:when>
            </xsl:choose>
            <xsl:apply-templates select="@textId"/>
        </xsl:copy>
    </xsl:template>
    <xsl:template match="fix">
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <xsl:apply-templates select="components"/>
            <xsl:apply-templates select="messages"/>
        </xsl:copy>
    </xsl:template>
    <xsl:template match="components">
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>
    <xsl:template match="component">
        <xsl:choose>
            <xsl:when test="@repeating = '1'">
                <xsl:apply-templates/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:copy>
                    <xsl:apply-templates select="@* except @type except @textId"/>
                    <xsl:apply-templates/>
                    <xsl:apply-templates select="@textId"/>
                </xsl:copy>
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
                           select="//field[@id = current()/@id]/@name"/>
            <xsl:attribute name="category" select="../@category"/>
            <xsl:attribute name="abbrName" select="../@abbrName"/>
            <xsl:apply-templates/>
            <xsl:apply-templates select="@textId"/>
        </group>
    </xsl:template>
    <xsl:template match="messages">
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>
    <xsl:template match="message">
        <xsl:copy>
            <xsl:apply-templates select="@* except @textId"/>
            <xsl:apply-templates/>
            <xsl:apply-templates select="@textId"/>
        </xsl:copy>
    </xsl:template>
    <xsl:template match="componentRef">
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
        </xsl:copy>
    </xsl:template>
    <xsl:template match="fieldRef">
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
        </xsl:copy>
    </xsl:template>
    <xsl:template match="@enumDatatype">
        <xsl:variable name="fieldName" select="//field[@id = current()]/@name"/>
        <xsl:attribute name="codeSet" select="concat($fieldName, 'CodeSet')"/>
    </xsl:template>
    <!-- name changes -->
    <xsl:template match="@minInclusive">
        <xsl:attribute name="lowerBound">
            <xsl:value-of select="current()"/>
        </xsl:attribute>
    </xsl:template>
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
        <xsl:element name="fixr:annotation">
            <xsl:for-each select="fn:key('phrases-key', ../@textId, $phrases-doc)//text">
                <xsl:element name="fixr:documentation">
                    <xsl:apply-templates select="@purpose"/>
                    <xsl:value-of select="."/>
                </xsl:element>
            </xsl:for-each>
        </xsl:element>
    </xsl:template>

    <xsl:template match="@purpose">
        <xsl:attribute name="purpose">
            <xsl:value-of select="."/>
        </xsl:attribute>
    </xsl:template>
</xsl:stylesheet>