#!/usr/bin/dot -O -Tpng
digraph ${name} {

<#list model.uniqueEntryById as node>
${node.getId()} [shape="Mrecord" label="{${node.getType()}|${(node.getName())!"NA"}}"]
</#list>

<#list model.produced as produces>
${produces.getSource().getId()} -> ${produced.getTarget().getId()}
</#list>

<#list model.consumed as consumes>
${consumes.getSource().getId()} -> ${consumed.getTarget().getId()}
</#list>

}

