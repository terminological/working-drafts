#!/usr/bin/dot -O -Tpng
digraph ${name} {

<#list model.uniqueEntryByType as node>
${node.id} [shape="box" label="${node.type}"</tr></table>>]
</#list>

<#list model.uniquePairAndCount as entry>
${entry.first.source.id} -> ${entry.first.target.id} [label = "${entry.second}"]
</#list>

}

