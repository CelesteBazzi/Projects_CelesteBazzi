using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using TMPro;
public class trailManager : MonoBehaviour
{
    public TrailRenderer trailRend;
    [SerializeField]
    public TMP_Text draw_text;
    public void ToggleDraw()
    {
        if(!trailRend.emitting){
            trailRend.emitting=true;
            draw_text.text="Stop";
        }
        else{
            trailRend.emitting=false;
            draw_text.text="Draw";
        }
    }
}
