using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.SceneManagement;

public class CheckWin : MonoBehaviour
{

    public GameObject winCanvasPrefab;
    public Transform target;

//if the turtle reaches the finish, the "you won" canvas is shown
    public void OnTriggerEnter(Collider other)
    {
        if(other.transform == target){
            var winCanvas = Instantiate(winCanvasPrefab, new Vector3(0, 0, 0), Quaternion.identity);
            
        }   
    }


    public void PlayAgain(){
        SceneManager.LoadScene(SceneManager.GetActiveScene().buildIndex);
    }

    public void GoToMenu(){
        SceneManager.LoadScene(0);
    }


}
