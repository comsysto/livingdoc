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
    <#assign typename=part.getDeclaringType().simpleName/>
    activate ${typename}
    <#list part.parse() as methodCall>
        ${methodCall}
    </#list>
    deactivate ${typename}
</#list>
@enduml
