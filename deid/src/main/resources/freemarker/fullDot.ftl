#!/usr/bin/dot -O -Tpng
digraph ${name} {

<#list model.getUniqueEntryById() as node>
${node.getId()} [shape="Mrecord" label="{${node.getType()}|${(node.getName())!"NA"}}"]
</#list>

<#list model.getProduced() as produces>
${produces.getSource().getId()} -> ${produces.getTarget().getId()}
</#list>

<#list model.getConsumed() as consumes>
${consumes.getSource().getId()} -> ${consumes.getTarget().getId()}
</#list>

}

