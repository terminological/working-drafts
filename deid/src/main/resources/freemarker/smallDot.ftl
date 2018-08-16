#!/usr/bin/dot -O -Tpng
digraph ${name} {

<#list model.uniqueEntryByType as node>
${node.id} [shape="Mrecord" label=<<table><tr>${node.type}</tr><tr>${node.name!none}</tr></table>>]
</#list>

<#list model.uniquePairAndCount as entry>
${entry.first.source.id} -> ${entry.first.target.id} [label = ${entry.second}]
</#list>

}

