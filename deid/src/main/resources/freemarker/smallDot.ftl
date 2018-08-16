#!/usr/bin/dot -O -Tpng
digraph ${name} {

<#list model.getUniqueEntryByType() as node>
${node.getTypeId()} [shape="box" label="${node.getType()}"</tr></table>>]
</#list>

<#list model.getUniquePairAndCount() as k, v>
${k.getSource().getTypeId()} -> ${k.getTarget().getTypeId()} [label = "${v}"]
</#list>

}

