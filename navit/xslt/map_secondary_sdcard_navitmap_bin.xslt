<?xml version="1.0"?>
<xsl:transform version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xi="http://www.w3.org/2001/XInclude">
	<xsl:template match="/config/navit/mapset/xi:include">
		<map type="binfile" enabled="no" data="/sdcard/zanavi/maps/bordersX.bin" />
		<xsl:text>&#x0A;                        </xsl:text>
		<map type="binfile" enabled="no" data="/sdcard/zanavi/maps/coastline.bin" />
		<xsl:text>&#x0A;                        </xsl:text>
		<map type="binfile" enabled="no" data="/sdcard/zanavi/maps/navitmap_001.bin" />
		<xsl:text>&#x0A;                        </xsl:text>
		<map type="binfile" enabled="no" data="/sdcard/zanavi/maps/navitmap_002.bin" />
		<xsl:text>&#x0A;                        </xsl:text>
		<map type="binfile" enabled="no" data="/sdcard/zanavi/maps/navitmap_003.bin" />
		<xsl:text>&#x0A;                        </xsl:text>
		<map type="binfile" enabled="no" data="/sdcard/zanavi/maps/navitmap_004.bin" />
		<xsl:text>&#x0A;                        </xsl:text>
	</xsl:template>
</xsl:transform>
