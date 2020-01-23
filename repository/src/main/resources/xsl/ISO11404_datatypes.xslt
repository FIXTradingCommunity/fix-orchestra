<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" 
xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
xmlns:xs="http://www.w3.org/2001/XMLSchema" 
xmlns:fn="http://www.w3.org/2005/xpath-functions"
	xmlns:fixr="http://fixprotocol.io/2016/fixrepository" xmlns:dc="http://purl.org/dc/elements/1.1" exclude-result-prefixes="fn dc xs">
	<!-- Enriches datatypes with mappings for General Purpose Datatypes ISO11404 -->
	<xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>
	<xsl:namespace-alias stylesheet-prefix="#default" result-prefix="fixr"/>
	<xsl:template match="/">
        <xsl:apply-templates/>
    </xsl:template>
    <xsl:template match="fixr:repository">
		<xsl:copy>
			<xsl:copy-of select="@*"/>
			<xsl:copy-of select="fixr:metadata"/>
			<xsl:copy-of select="fixr:codeSets"/>
			<xsl:copy-of select="fixr:abbreviations"/>
			<xsl:apply-templates select="fixr:datatypes"/>
			<xsl:copy-of select="fixr:categories"/>
			<xsl:copy-of select="fixr:groups"/>
			<xsl:copy-of select="fixr:sections"/>
			<xsl:copy-of select="fixr:fields"/>
			<xsl:copy-of select="fixr:actors"/>
			<xsl:copy-of select="fixr:components"/>
			<xsl:copy-of select="fixr:messages"/>
			<xsl:copy-of select="fixr:concepts"/>
		</xsl:copy>
    </xsl:template>
    <xsl:template match="fixr:datatypes">
		<xsl:copy>
				<xsl:apply-templates/>
		</xsl:copy>
    </xsl:template>
    <xsl:template match="fixr:datatype">
		<xsl:copy>
			<xsl:copy-of select="@*"/>
			<xsl:copy-of select="fixr:mappedDatatype"/>
			<xsl:choose>
				<xsl:when test="@name='int'">
				<fixr:mappedDatatype standard="ISO11404" base="integer" builtin="1"/>
				</xsl:when>
				<xsl:when test="@name='Length'">
				<fixr:mappedDatatype standard="ISO11404" base="size" builtin="1"/>
				</xsl:when>
				<xsl:when test="@name='TagNum'">
				<fixr:mappedDatatype standard="ISO11404" base="ordinal" builtin="1"/>
				</xsl:when>
				<xsl:when test="@name='SeqNum'">
				<fixr:mappedDatatype standard="ISO11404" base="ordinal" builtin="1"/>
				</xsl:when>
				<xsl:when test="@name='NumInGroup'">
				<fixr:mappedDatatype standard="ISO11404" base="size" builtin="1"/>
				</xsl:when>
				<xsl:when test="@name='DayOfMonth'">
				<fixr:mappedDatatype standard="ISO11404" base="range" parameter="integer" minInclusive="1" maxInclusive="31" builtin="0"/>
				</xsl:when>		
				<xsl:when test="@name='float'">
				<fixr:mappedDatatype standard="ISO11404" base="real" builtin="1"/>
				</xsl:when>
				<xsl:when test="@name='Qty'">
				<fixr:mappedDatatype standard="ISO11404" base="scaled" parameter="radix=10" builtin="0"/>
				</xsl:when>
				<xsl:when test="@name='Price'">
				<fixr:mappedDatatype standard="ISO11404" base="scaled" parameter="radix=10" builtin="0"/>
				</xsl:when>
				<xsl:when test="@name='PriceOffset'">
				<fixr:mappedDatatype standard="ISO11404" base="scaled" parameter="radix=10" builtin="0"/>
				</xsl:when>
				<xsl:when test="@name='Amt'">
				<fixr:mappedDatatype standard="ISO11404" base="scaled" parameter="radix=10" builtin="0"/>
				</xsl:when>
				<xsl:when test="@name='Percentage'">
				<fixr:mappedDatatype standard="ISO11404" base="scaled" parameter="radix=10" builtin="0"/>
				</xsl:when>
				<xsl:when test="@name='char'">
				<fixr:mappedDatatype standard="ISO11404" base="character" parameter="repertoire=US-ASCII" builtin="1"/>
				</xsl:when>
				<xsl:when test="@name='Boolean'">
				<fixr:mappedDatatype standard="ISO11404" base="boolean" builtin="1"/>
				</xsl:when>
				<xsl:when test="@name='String'">
				<fixr:mappedDatatype standard="ISO11404" base="characterstring" parameter="repertoire=US-ASCII" builtin="1"/>
				</xsl:when>
				<xsl:when test="@name='MultipleCharValue'">
				<fixr:mappedDatatype standard="ISO11404" base="set" element="character" builtin="0"/>
				</xsl:when>
				<xsl:when test="@name='MultipleStringValue'">
				<fixr:mappedDatatype standard="ISO11404" base="set" element="characterstring" builtin="0"/>
				</xsl:when>
				<xsl:when test="@name='Country'">
				<fixr:mappedDatatype standard="ISO11404" base="array" element="character" parameter="index-lowerbound=1;index-upperbound=2" builtin="0"/>
				<fixr:annotation>
					<fixr:appinfo specUrl="http://www.iso.org/iso/home/store/catalogue_tc/catalogue_detail.htm?csnumber=63545">ISO 3166-1:2013 Codes for the representation of names of countries and their subdivisions -- Part 1: Country codes</fixr:appinfo>
				</fixr:annotation>
				</xsl:when>
				<xsl:when test="@name='Currency'">
				<fixr:mappedDatatype standard="ISO11404" base="array" element="character" parameter="index-lowerbound=1;index-upperbound=3" builtin="0"/>
				<fixr:annotation>
					<fixr:appinfo specUrl="http://www.iso.org/iso/home/store/catalogue_tc/catalogue_detail.htm?csnumber=46121">ISO 4217:2015 Codes for the representation of currencies</fixr:appinfo>
				</fixr:annotation>
				</xsl:when>
				<xsl:when test="@name='Exchange'">
				<fixr:mappedDatatype standard="ISO11404" base="array" element="character" parameter="index-lowerbound=1;index-upperbound=4" builtin="0"/>
				<fixr:annotation>
					<fixr:appinfo specUrl="http://www.iso.org/iso/home/store/catalogue_tc/catalogue_detail.htm?csnumber=61067">ISO 10383:2012 Codes for exchanges and market identification (MIC)</fixr:appinfo>
				</fixr:annotation>
				</xsl:when>
				<xsl:when test="@name='MonthYear'">
				<fixr:mappedDatatype standard="ISO11404" base="characterstring" builtin="0"/>
				</xsl:when>	
				<xsl:when test="@name='UTCTimestamp'">
				<fixr:mappedDatatype standard="ISO11404" base="time" parameter="time-unit=microsecond" builtin="1"/>
				</xsl:when>			
				<xsl:when test="@name='UTCTimeOnly'">
				<fixr:mappedDatatype standard="ISO11404" base="time" parameter="time-unit=microsecond" builtin="1"/>
				</xsl:when>		
				<xsl:when test="@name='UTCDateOnly'">
				<fixr:mappedDatatype standard="ISO11404" base="time" parameter="time-unit=day" builtin="1"/>
				</xsl:when>	
				<xsl:when test="@name='LocalMktDate'">
				<fixr:mappedDatatype standard="ISO11404" base="time" parameter="time-unit=day" builtin="1"/>
				</xsl:when>	
				<xsl:when test="@name='TZTimeOnly'">
				<fixr:mappedDatatype standard="ISO11404" base="time" parameter="time-unit=microsecond" builtin="1"/>
				</xsl:when>	
				<xsl:when test="@name='TZTimestamp'">
				<fixr:mappedDatatype standard="ISO11404" base="time" parameter="time-unit=microsecond" builtin="1"/>
				</xsl:when>	
				<xsl:when test="@name='data'">
				<fixr:mappedDatatype standard="ISO11404" base="octetstring" builtin="1"/>
				</xsl:when>	
				<xsl:when test="@name='Tenor'">
				<fixr:mappedDatatype standard="ISO11404" base="characterstring" builtin="0"/>
				</xsl:when>	
				<xsl:when test="@name='Reserved100Plus'">
				<fixr:mappedDatatype standard="ISO11404" base="range" parameter="integer" minInclusive="100" builtin="0"/>
				</xsl:when>	
				<xsl:when test="@name='Reserved1000Plus'">
				<fixr:mappedDatatype standard="ISO11404" base="range" parameter="integer" minInclusive="1000" builtin="0"/>
				</xsl:when>	
				<xsl:when test="@name='Reserved4000Plus'">
				<fixr:mappedDatatype standard="ISO11404" base="range" parameter="integer" minInclusive="4000" builtin="0"/>
				</xsl:when>	
				<xsl:when test="@name='XMLData'">
				<fixr:mappedDatatype standard="ISO11404" base="characterstring" builtin="1"/>
				</xsl:when>	
				<xsl:when test="@name='Language'">
				<fixr:mappedDatatype standard="ISO11404" base="array" element="character" parameter="index-lowerbound=1;index-upperbound=2" builtin="0"/>
				<fixr:annotation>
					<fixr:appinfo specUrl="http://www.iso.org/iso/home/store/catalogue_tc/catalogue_detail.htm?csnumber=22109">ISO 639-1:2002 Codes for the representation of names of languages -- Part 1: Alpha-2 code</fixr:appinfo>
				</fixr:annotation>
				</xsl:when>
				<xsl:when test="@name='LocalMktTime'">
				<fixr:mappedDatatype standard="ISO11404" base="time" parameter="time-unit=microsecond" builtin="1"/>
				</xsl:when>				
			</xsl:choose>
		</xsl:copy>
    </xsl:template>
</xsl:stylesheet>
