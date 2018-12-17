<!DOCTYPE html>
<meta charset="utf-8">
<head>
  <title>Force layout (with links)</title>
</head>

<style>
circle {
  fill: cadetblue;
}
line {
  stroke: #ccc;
}
text {
  text-anchor: middle;
  font-family: "Helvetica Neue", Helvetica, sans-serif;
  fill: #666;
  font-size: 16px;
}
</style>

<body>
  <div id="content">
    <svg width="400" height="300">
      <g class="links"></g>
      <g class="nodes"></g>
    </svg>
  </div>
  <input id="dl"
      name="downloadButton"
      type="button"
      value="Download SVG" />
  <script>

  <script src="https://cdnjs.cloudflare.com/ajax/libs/d3/4.2.2/d3.min.js"></script>
  <script src="http://edeno.github.io/d3-save-svg/assets/d3-save-svg.min.js" charset="utf-8"></script>

  <script>
var width = 400, height = 300

${data}

var simulation = d3.forceSimulation(nodes)
  .force('charge', d3.forceManyBody().strength(-100))
  .force('center', d3.forceCenter(width / 2, height / 2))
  .force('link', d3.forceLink()
  		.links(links)
  		.id(function(d) {return d.id;})
  		.strength(function(link) {return link.weight;})
  )
  .on('tick', ticked);

function updateLinks() {
  var u = d3.select('.links')
    .selectAll('line')
    .data(links)

  u.enter()
    .append('line')
    .merge(u)
    .attr('x1', function(d) {
      return d.source.x
    })
    .attr('y1', function(d) {
      return d.source.y
    })
    .attr('x2', function(d) {
      return d.target.x
    })
    .attr('y2', function(d) {
      return d.target.y
    })

  u.exit().remove()
}

function updateNodes() {
  u = d3.select('.nodes')
    .selectAll('text')
    .data(nodes)

  u.enter()
    .append('text')
    .text(function(d) {
      return d.name
    })
    .merge(u)
    .attr('x', function(d) {
      return d.x
    })
    .attr('y', function(d) {
      return d.y
    })
    .attr('dy', function(d) {
      return 5
    })

  u.exit().remove()
}

function ticked() {
  updateLinks()
  updateNodes()
}

//Add a save SVG button
d3.select('#dl').on('click', function() {
    d3_save_svg.save(d3.select('svg').node());
});

  </script>
</body>
</html>