<#macro renderField field>
<#-- @ftlvariable name="association" type="com.comsysto.livingdoc.s0t.model.FieldModel" -->
    <@renderModifier field.accessModifier/>${field.type.name.simpleName}<@renderTypeParameters field.typeParameters/> ${field.name}
</#macro>

<#macro renderModifier modifier><#rt>
    <#if modifier.name()="PUBLIC">+ <#lt><#rt>
    <#elseif modifier.name()="PROTECTED"># <#lt><#rt>
    <#elseif modifier.name()="PACKAGE">~ <#lt><#rt>
    <#elseif modifier.name()="PRIVATE">- <#lt><#rt>
    <#else><#lt><#rt>
    </#if><#lt><#rt>
</#macro>

<#macro renderTypeParameters params><#rt>
<#-- @ftlvariable name="params" type="java.util.List<com.comsysto.livingdoc.s0t.model.TypeRef>" --><#rt>
    <#if params?size gt 0><#lt><#rt>
        <<#list params as p>${p.name.simpleName}<#sep>, </#sep></#list>><#lt><#rt>
    </#if><#lt><#rt>
</#macro>

