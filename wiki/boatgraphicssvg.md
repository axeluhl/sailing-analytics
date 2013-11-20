# Create boat graphics for the 2D race viewer

This page describes how to create the SVG graphics for different boat classes which can be used by the 2D race viewer to draw boat graphics directly to a HTML Canvas.

## Inkscape setup

We used the free Inkscape (http://inkscape.org/) as the tool to create and edit SVG graphics.
However, there are some settings to change in Inkscape before you should start working with it.

## Conversion of SVG graphics into a Canvas drawing commands

You can use an online tool (SVG to HTML5 Canvas Converter: http://www.professorcloud.com/svg-to-canvas/) to convert the SVG graphics to a list of drawing commands. So for example a SVG line<br/>
&lt;line x1="1050" y1="651" x2="1050" y2="49" id="line75" /&gt;<br/>
will be converted to something like<br/>
ctx.beginPath();<br/>
ctx.moveTo(1050,651);<br/>
ctx.lineTo(1050,49);<br/>
ctx.fill();<br/>
ctx.stroke();<br/>
This is acutually javascript code, but can directly be used as Java code as well.


