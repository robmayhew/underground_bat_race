extends Node2D


@export var speed = 100
@export var slide = 400


var time_elapsed = 0.0
var initial_x = 0.0

# Called when the node enters the scene tree for the first time.
func _ready() -> void:
	initial_x = position.x


# Called every frame. 'delta' is the elapsed time since the previous frame.
func _process(delta: float) -> void:
	time_elapsed += delta
	# Oscillate left and right using sine wave
	var offset = sin(time_elapsed * speed / 100.0) * slide
	position.x = initial_x + offset
