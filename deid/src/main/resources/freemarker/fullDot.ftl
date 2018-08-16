#!/usr/bin/dot -O -Tpng
digraph ${name} {

<#list model.uniqueEntryById as node>
${node.id} [shape="Mrecord" label="{${node.type}|${(node.name)!"NA"}}"]
</#list>

<#list model.produced as produced>
${produced.source.id} -> ${produced.target.id}
</#list>

<#list model.consumed as consumed>
${consumed.source.id} -> ${consumed.target.id}
</#list>

}

