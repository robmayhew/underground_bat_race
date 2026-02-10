extends Node2D


var start_bat_position 
var start_water_position
var pipe_scene:PackedScene
var pipe_positions = [-800]
# Called when the node enters the scene tree for the first time.
func _ready() -> void:
	start_bat_position = $Bat.position
	start_water_position = $Waterline.position
	pipe_scene = ResourceLoader.load("res://pipe.tscn")
	app_pipes()
	pass # Replace with function body.


# Called every frame. 'delta' is the elapsed time since the previous frame.
func _process(delta: float) -> void:
	pass


func app_pipes() -> void:
	var y = pipe_positions[-1]
	for i in range(4):
		y = y + 100 + randf_range(300,400)
		pipe_positions.append(y)
		var pipe = pipe_scene.instantiate()
		pipe.position.y = -1 * y;
		pipe.position.x = 100 - randi_range(1,200)
		pipe.speed = randi_range(100,150)
		add_child(pipe)
		
