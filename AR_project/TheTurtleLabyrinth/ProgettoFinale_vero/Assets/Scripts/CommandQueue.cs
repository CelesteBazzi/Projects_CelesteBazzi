using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;
using TMPro;

public class CommandQueue : MonoBehaviour
{
    private enum Movement {
        Forward,
        Left,
        Right
    };
    
    public TMP_InputField repetitions;
    private List<Movement> commands = new List<Movement>();
    public control turtleControl;
    public TrailRenderer trailRend;
    public BoardManager boardManager;

    void Start(){
        boardManager.WriteWhile();
    }


    public void AddCommand(string move){
        switch (move){
            case "Forward":
                commands.Add(Movement.Forward);
                break;
            case "Left 45":
                commands.Add(Movement.Left);
                break;
            case "Right 45":
                commands.Add(Movement.Right);
                break;
        }
    }

    IEnumerator ExecuteCommands(int repetitions)
    {
        for (int i = 0; i < repetitions; i++)
        {
            foreach (var command in commands)
            {
                switch (command)
                {
                    case Movement.Forward:
                        trailRend.emitting = true;
                        turtleControl.MoveForward();
                        break;
                    case Movement.Left:
                        turtleControl.RotateLeft();
                        break;
                    case Movement.Right:
                        turtleControl.RotateRight();
                        break;
                }
                //Wait for 0.5 seconds to execute the next action
                yield return new WaitForSecondsRealtime(0.5f);
            }
        }
        commands = new List<Movement>();
        boardManager.NewLine();
        boardManager.WriteWhile();
    }

    public void Execute(){
        var text = repetitions.text;
        int n;
        int.TryParse(text, out n);
        //Starts the coroutine to execute the commands sequentially
        StartCoroutine(ExecuteCommands(n));
    }

    public void CheckRepetitions(){
        this.GetComponent<Button>().interactable = !string.IsNullOrEmpty(repetitions.text);
    }
}
