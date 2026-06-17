using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.SceneManagement;

public class MainMenu : MonoBehaviour
{
    public void Game1()
    {
        SceneManager.LoadScene(1);
    }
    
    public void Game2()
    {
        SceneManager.LoadScene(2);
    }
    
    public void TutoGame3()
    {
        SceneManager.LoadScene(3);
    }
    public void Game3()
    {
        SceneManager.LoadScene(4);
    }
}
