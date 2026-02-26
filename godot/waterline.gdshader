shader_type canvas_item;

uniform float time_scale = 1.0;
uniform float wave_strength = 0.005;
uniform float wave_speed = 2.0;
uniform vec4 water_color : source_color = vec4(0.0, 0.4, 0.9, 1.0);






void fragment() {
    float t = TIME * wave_speed * time_scale;

    vec2 distorted_uv = UV;
    distorted_uv.x += sin(UV.y * 20.0 + t) * wave_strength;
    distorted_uv.y += cos(UV.x * 9.0 + t) * wave_strength;
	
	distorted_uv = clamp(distorted_uv, vec2(0.0), vec2(0.8,0.8));
	
    vec4 base = texture(TEXTURE, distorted_uv);

    // Blend toward water color
    vec3 tinted = mix(base.rgb, water_color.rgb, 0.2);

    // Slight blue boost
    tinted.b *= 1.2;

    // Slight darkening for depth feel
    tinted *= 0.9;

     COLOR = vec4(tinted, base.a);
	
}
