<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet version="1.0"
            xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
            xmlns:c="http://www.esei.uvigo.es/dai/hybridserver">

    <xsl:output method="html" indent="yes" encoding="UTF-8"/>

    <xsl:template match="/">
        <xsl:text disable-output-escaping="yes">&lt;!DOCTYPE HTML&gt;</xsl:text>
        <html>
            <head>
                <title>Configuration</title>
            </head>
            <body>
                <h1>Configuration</h1>
                <xsl:apply-templates select="c:configuration/c:connections" />
                <xsl:apply-templates select="c:configuration/c:database" />
                <xsl:apply-templates select="c:configuration/c:servers" />
            </body>
        </html>
    </xsl:template>

    <xsl:template match="c:connections">
        <h2>Connections</h2>
        <p>http: <xsl:value-of select="c:http"/> </p>
        <p>webservice: <xsl:value-of select="c:webservice"/> </p>
        <p>numClients: <xsl:value-of select="c:numClients"/> </p>
    </xsl:template>

    <xsl:template match="c:database">
        <h2>Database</h2>
        <p>user: <xsl:value-of select="c:user"/></p>
        <p>password: <xsl:value-of select="c:password"/></p>
        <p>url: <xsl:value-of select="c:url"/></p>
    </xsl:template>

    <xsl:template match="c:servers">
        <h2>Servers</h2>
        <xsl:apply-templates select="c:server" />
    </xsl:template>

    <xsl:template match="c:server">
        <h3><xsl:value-of select="@name"/></h3>
        <p>wsdl: <xsl:value-of select="@wsdl"/></p>
        <p>namespace: <xsl:value-of select="@namespace"/></p>
        <p>service: <xsl:value-of select="@service"/></p>
        <p>httpAddress: <xsl:value-of select="@httpAddress"/></p>
    </xsl:template>

</xsl:stylesheet>