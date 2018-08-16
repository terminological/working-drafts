#!/usr/bin/dot -O -Tpng
digraph ${name} {

<#list model.getUniqueEntryByType() as node>
${node.getTypeId()} [shape="box" label="${node.getType()}"</tr></table>>]
</#list>

<#list model.getUniquePairAndCount() as entry>
${entry.getKey().getSource().getTypeId()} -> ${entry.getKey().getTarget().getTypeId()} [label = "${entry.getValue()}"]
</#list>

}

