using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;
using TMPro;

public class BoardManager : MonoBehaviour
{

    public TMP_Text scroll_text;
    public ScrollRect scrollRect;

    private static int Count;

    void Start(){
        Count = 1;
    }

    public void WriteAction(string action){
        scroll_text.text += $"{Count}. {action}\n";
        if(Count>=7){
            scrollRect.velocity = new Vector2(0f, 100f);
        }
        Count++;
    }

    public void WriteActionWhile(string action){
        scroll_text.text += $"  {action}\n";
        if(Count>=5){
            scrollRect.velocity = new Vector2(0f, 100f);
        }
        Count++;
    }


    public void NewLine(){
        scroll_text.text += "\n";
    }
    public void WriteWhile(){
        scroll_text.text += "while:\n";
    }
}
