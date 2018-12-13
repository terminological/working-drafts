<!DOCTYPE html>
<meta charset="utf-8">
<style>

.link {
  stroke: #ccc;
}

.node text {
  pointer-events: none;
  font: 10px sans-serif;
}

</style>
<body>
<!-- D3.js -->
		<script src="https://cdnjs.cloudflare.com/ajax/libs/d3/3.5.6/d3.min.js" charset="utf-8"></script>
<script>

${data}

var width = 960,
    height = 500

var svg = d3.select("body").append("svg")
    .attr("width", width)
    .attr("height", height);

var force = d3.layout.force()
    .gravity(0.05)
    .linkStrength(function(d) {return d.weight;})
    .distance(100)
    .charge(-100)
    .size([width, height]);

force
     .nodes(graph.nodes)
      .links(graph.links)
      .start();

  var link = svg.selectAll(".link")
      .data(graph.links)
    .enter().append("line")
      .attr("class", "link");

  var node = svg.selectAll(".node")
      .data(graph.nodes)
      .enter().append("svg:circle")
      .attr("class", "node")
    	.attr("cx", function(d) { return d.x; }) //x
    	.attr("cy", function(d) { return d.y; }) //y
    	.attr("r", 8)
  	    .style("fill", function(d, i) {
		      return fill[parseInt((d.in+1)/3)];
    	})
      .call(force.drag);
      
  node.append("text")
      .attr("dx", 12)
      .attr("dy", ".35em")
      .text(function(d) { return d.label });

  force.on("tick", function() {
    link.attr("x1", function(d) { return d.source.x; })
        .attr("y1", function(d) { return d.source.y; })
        .attr("x2", function(d) { return d.target.x; })
        .attr("y2", function(d) { return d.target.y; });

    node.attr("transform", function(d) { return "translate(" + d.x + "," + d.y + ")"; });
  });


</script>

