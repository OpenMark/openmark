<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<xsl:variable name="LETTERS">abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZαβγδεζηθικλμνξοπρςστυφχψω</xsl:variable>
<xsl:param name="ITALIC">y</xsl:param>
 
<xsl:template match="/int_equation">

	<span class="textequation">
		<xsl:attribute name="alt"><xsl:apply-templates mode="alt"/></xsl:attribute>
		<xsl:apply-templates/>
  </span>	
</xsl:template>

<xsl:template match="int_text">
	<xsl:choose>
		<xsl:when test="ancestor::mbox">
			<xsl:value-of select="."/>
		</xsl:when>
		<xsl:otherwise>
			<xsl:call-template name="writetext">
				<xsl:with-param name="TEXT" select="string(.)"/>
			</xsl:call-template>			
		</xsl:otherwise>
	</xsl:choose>
</xsl:template>

<xsl:template match="int_text" mode="alt">
	<xsl:value-of select="."/>
</xsl:template>


<xsl:template name="writetext">
	<xsl:param name="TEXT"/>
	
	<xsl:variable name="FIRST" select="substring($TEXT,1,1)"/>
	<xsl:choose>
		<xsl:when test="contains($LETTERS,$FIRST) and $ITALIC='y'">
			<i><xsl:value-of select="$FIRST"/></i>
		</xsl:when>	
		<xsl:otherwise>
			<xsl:value-of select="$FIRST"/>
		</xsl:otherwise>
	</xsl:choose>
	<xsl:text></xsl:text>
	
	<xsl:if test="string-length($TEXT) &gt; 1">
		<xsl:call-template name="writetext">
			<xsl:with-param name="TEXT" select="substring($TEXT,2)"/>
		</xsl:call-template>
	</xsl:if>
	
</xsl:template>


<xsl:template match="int_sup">
	<sup><xsl:apply-templates/></sup>
</xsl:template>

<xsl:template match="int_sup" mode="alt">
	<xsl:text> to the power </xsl:text><xsl:apply-templates mode="alt"/><xsl:text> </xsl:text>
</xsl:template>

<xsl:template match="int_sub">
	<sub><xsl:apply-templates/></sub>
</xsl:template>

<xsl:template match="int_sub" mode="alt">
	<xsl:text> subscript </xsl:text><xsl:apply-templates mode="alt"/><xsl:text> </xsl:text>
</xsl:template>
	
</xsl:stylesheet>
