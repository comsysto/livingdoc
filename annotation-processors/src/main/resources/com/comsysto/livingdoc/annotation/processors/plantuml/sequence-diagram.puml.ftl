<#-- @ftlvariable name="title" type="String" -->
<#-- @ftlvariable name="parts" type="java.util.List<com.comsysto.livingdoc.annotation.processors.plantuml.model.ExecutablePart>" -->
<#import "seqs.part.ftl" as seqs>
@startuml
<#list getIncludeFiles() as includeFile>
    !include ${includeFile}
</#list>
<#if title??>
    title ${title}
</#if>
<#list typeNames() as typeName>
    participant ${typeName}
</#list>

<#list parts as part>
${part.render()}
</#list>
@enduml
