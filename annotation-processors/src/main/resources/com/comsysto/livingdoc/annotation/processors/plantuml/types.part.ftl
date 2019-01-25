<#macro type part>
<#-- @ftlvariable name="part" type="com.comsysto.livingdoc.annotation.processors.plantuml.ClassDiagramPart" -->
    <#if part.isInterface()>
        <#local typeDeclaration="interface">
    <#elseif part.isAbstract()>
        <#local typeDeclaration="abstract class">
    <#elseif part.isEnum()>
        <#local typeDeclaration="enum">
    <#else>
        <#local typeDeclaration="class">
    </#if>
${typeDeclaration} ${part.name}
    <#list part.notes as note>
note ${note.position()?lower_case} of ${part.name}
${note.body()}
end note

    </#list>
</#macro>


<#macro inheritanceAssociation association>
<#-- @ftlvariable name="association" type="com.comsysto.livingdoc.annotation.processors.plantuml.AssociationsPart" -->
${association.left.simpleName} <|-- ${association.right.simpleName}
</#macro>