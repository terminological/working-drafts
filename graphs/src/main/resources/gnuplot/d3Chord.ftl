<!DOCTYPE html>
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html;charset=utf-8"/>
		<title>${config.getTitle()}</title>
	
		<!-- D3.js -->
		<script src="https://cdnjs.cloudflare.com/ajax/libs/d3/3.5.6/d3.min.js" charset="utf-8"></script>
		
		<script src="http://edeno.github.io/d3-save-svg/assets/d3-save-svg.min.js" charset="utf-8"></script>
		
		<!-- Google Fonts -->
		<link href='https://fonts.googleapis.com/css?family=Lato:400,900' rel='stylesheet' type='text/css'>
		
		<style>
			body {
			  font-size: 12px;
			  font-family: 'Lato', sans-serif;
			  text-align: center;
			  fill: #2B2B2B;
			  cursor: default;
			}
			
			@media (min-width: 600px) {
				#chart{
					font-size: 16px;
				}
			}
		</style>
	</head>
	<body>
	
		<div id = "chart"></div>
		<input id="dl"
      name="downloadButton"
      type="button"
      value="Download SVG" />
		<script>
////////////////////////////////////////////////////////////
//////////////////////// Set-Up ////////////////////////////
////////////////////////////////////////////////////////////

var margin = {left:90, top:90, right:90, bottom:90},
	width = Math.min(window.innerWidth, 1000) - margin.left - margin.right,
    height = Math.min(window.innerWidth, 1000) - margin.top - margin.bottom,
    innerRadius = Math.min(width, height) * .3,
    outerRadius = innerRadius * 1.1;
	
// var colors = ["#301E1E", "#083E77", "#342350", "#567235", "#8B161C", "#DF7C00"];
var opacityDefault = 0.8;

${data}

////////////////////////////////////////////////////////////
/////////// Create scale and layout functions //////////////
////////////////////////////////////////////////////////////

var colors = d3.scale.ordinal()
    .domain(d3.range(names.length))
	.range(colors);

var chord = d3.layout.chord()
    .padding(.01)
    .sortChords(d3.descending)
	.matrix(matrix);
		
var arc = d3.svg.arc()
    .innerRadius(innerRadius*1.01)
    .outerRadius(outerRadius);

var path = d3.svg.chord()
	.radius(innerRadius);
	
////////////////////////////////////////////////////////////
////////////////////// Create SVG //////////////////////////
////////////////////////////////////////////////////////////
	
var svg = d3.select("#chart").append("svg")
    .attr("width", width + margin.left + margin.right)
    .attr("height", height + margin.top + margin.bottom)
	.append("g")
    .attr("transform", "translate(" + (width/2 + margin.left) + "," + (height/2 + margin.top) + ")");
		
////////////////////////////////////////////////////////////
////////////////// Draw outer Arcs /////////////////////////
////////////////////////////////////////////////////////////

var outerArcs = svg.selectAll("g.group")
	.data(chord.groups)
	.enter().append("g")
	.attr("class", "group")
	.on("mouseover", fade(.1))
	.on("mouseout", fade(opacityDefault));

outerArcs.append("path")
	.style("fill", function(d) { return colors(d.index); })
	.attr("d", arc);
	
////////////////////////////////////////////////////////////
////////////////////// Append names ////////////////////////
////////////////////////////////////////////////////////////

//Append the label names on the outside
outerArcs.append("text")
  .each(function(d) { d.angle = (d.startAngle + d.endAngle) / 2; })
  .attr("dy", ".35em")
  .attr("class", "titles")
  .attr("text-anchor", function(d) { return d.angle > Math.PI ? "end" : null; })
  .attr("transform", function(d) {
		return "rotate(" + (d.angle * 180 / Math.PI - 90) + ")"
		+ "translate(" + (outerRadius + 10) + ")"
		+ (d.angle > Math.PI ? "rotate(180)" : "");
  })
  .text(function(d,i) { return names[i]; });
	
////////////////////////////////////////////////////////////
////////////////// Draw inner chords ///////////////////////
////////////////////////////////////////////////////////////
  
svg.selectAll("path.chord")
	.data(chord.chords)
	.enter().append("path")
	.attr("class", "chord")
	.style("fill", function(d) { return colors(d.source.index); })
	.style("opacity", opacityDefault)
	.attr("d", path);

////////////////////////////////////////////////////////////
////////////////// Extra Functions /////////////////////////
////////////////////////////////////////////////////////////

//Returns an event handler for fading a given chord group.
function fade(opacity) {
  return function(d,i) {
    svg.selectAll("path.chord")
        .filter(function(d) { return d.source.index != i && d.target.index != i; })
		.transition()
        .style("opacity", opacity);
  };
}//fade


d3.select('#dl').on('click', function() {
      var config = {
        filename: '${config.getOutputFile()}.svg',
      }
      d3_save_svg.save(d3.select('svg').node(), config);
    });

		</script>

  </body>
</html>

